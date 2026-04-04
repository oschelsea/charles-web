<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  NButton,
  NButtonGroup,
  NCard,
  NCheckbox,
  NDivider,
  NPopover,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NSpin,
  NTag,
  NTooltip
} from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import { type Layer, layerApi } from '@/service/api/xenon/layer';
import { buildCapabilitiesUrl, buildWmtsTileUrl } from '@/service/api/xenon/wmts';
import LeafletMap from '@/components/xenon-map/LeafletMap.vue';
import type { TileMatrixSetInfo, WmsLayerConfig, WmtsLayerConfig } from '@/components/xenon-map/LeafletMap.vue';
import IconLayers from '~icons/ion/layers-outline';
import IconSettings from '~icons/ion/settings-outline';
import IconInformation from '~icons/ion/information-circle-outline';

const route = useRoute();

const loading = ref(false);
const mapRef = ref<InstanceType<typeof LeafletMap>>();

const showLayerPopover = ref(false);
const showSettingsPopover = ref(false);
const showInfoPopover = ref(false);

const availableLayers = ref<Layer[]>([]);
const serviceType = ref<'WMS' | 'WMTS'>('WMS');
const selectedLayers = ref<string[]>([]);

const API_ORIGIN = `${window.location.protocol}//${window.location.host}`;
const API_BASE_URL = `${API_ORIGIN}${import.meta.env.VITE_APP_BASE_API || ''}`;
const CONTEXT_PATH = '/xenon';

const wmsUrl = ref(`${API_BASE_URL}${CONTEXT_PATH}/services`);
const wmsVersion = ref('1.3.0');
const wmsFormat = ref('image/png');
const wmsTransparent = ref(true);

const wmtsBaseUrl = ref(API_ORIGIN);
const wmtsTileMatrixSet = ref('EPSG:3857');
const wmtsFormat = ref('png');

const basemap = ref<'osm' | 'satellite' | 'terrain' | 'blank'>('satellite');
const basemapOptions: SelectOption[] = [
  { label: '🗺️ OpenStreetMap', value: 'osm' },
  { label: '🛰️ Esri Satellite', value: 'satellite' },
  { label: '🏔️ Esri Terrain', value: 'terrain' },
  { label: '⬜ Blank', value: 'blank' }
];

const parsedTileMatrixSet = ref<TileMatrixSetInfo | null>(null);
const parsedBounds = ref<[[number, number], [number, number]] | null>(null);
const statusLongitude = ref<number | null>(117);
const statusLatitude = ref<number | null>(35);
const statusZoom = ref<number | null>(5);

const wmsLayers = computed<WmsLayerConfig[]>(() => {
  if (serviceType.value !== 'WMS' || selectedLayers.value.length === 0) return [];

  return selectedLayers.value.map(layerName => ({
    name: layerName,
    url: `${API_BASE_URL}${CONTEXT_PATH}/services/${layerName}/wms`,
    layers: layerName,
    transparent: wmsTransparent.value,
    format: wmsFormat.value,
    version: wmsVersion.value
  }));
});

const wmtsLayers = computed<WmtsLayerConfig[]>(() => {
  if (serviceType.value !== 'WMTS' || selectedLayers.value.length === 0) return [];

  // 必须等待 TileMatrixSet 解析完成后再加载，否则会使用错误的 CRS
  const tileMatrixSet = parsedTileMatrixSet.value;
  if (!tileMatrixSet) return [];

  return selectedLayers.value.map(layerName => ({
    name: layerName,
    url: buildWmtsTileUrl(layerName, {
      baseUrl: wmtsBaseUrl.value,
      tileMatrixSet: wmtsTileMatrixSet.value,
      format: wmtsFormat.value
    }),
    attribution: 'Xenon WMTS',
    tileMatrixSet
  }));
});

const layerOptions = computed<SelectOption[]>(() =>
  availableLayers.value
    .filter(layer => layer.enabled && layer.type !== 'TILES3D' && layer.type !== 'TERRAIN')
    .map(layer => {
      const qualifiedName = layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
      const typeIcon = layer.type === 'ARCGIS_CACHE' || layer.type === 'GEOPACKAGE_TILES' ? '🗺️' : layer.type === 'VECTOR' ? '📐' : '🖼️';
      return {
        label: `${typeIcon} ${layer.workspaceName ? `${layer.workspaceName}:` : ''}${layer.title || layer.name}`,
        value: qualifiedName
      };
    })
);

// 根据图层类型自动推断服务类型
function inferServiceTypeFromLayer(layerName: string): 'WMS' | 'WMTS' | null {
  const layer = availableLayers.value.find(l => {
    const qualifiedName = l.workspaceName ? `${l.workspaceName}:${l.name}` : l.name;
    return qualifiedName === layerName;
  });

  if (!layer) return null;

  // 瓦片类图层默认使用 WMTS
  if (layer.type === 'ARCGIS_CACHE' || layer.type === 'GEOPACKAGE_TILES') {
    return 'WMTS';
  }

  // WMS 级联图层使用 WMS
  if (layer.type === 'WMS') {
    return 'WMS';
  }

  // 其他类型默认使用 WMS
  return 'WMS';
}

const layerSummaryText = computed(() => {
  if (selectedLayers.value.length === 0) return '未选择';
  if (selectedLayers.value.length === 1) return selectedLayers.value[0];
  return `${selectedLayers.value[0]} 等 ${selectedLayers.value.length} 个`;
});

const statusLongitudeText = computed(() => formatCoordinate(statusLongitude.value));
const statusLatitudeText = computed(() => formatCoordinate(statusLatitude.value));
const statusZoomText = computed(() => (statusZoom.value === null ? '--' : String(statusZoom.value)));

function getFirstQueryString(value: unknown): string | null {
  if (typeof value === 'string') return value;
  if (Array.isArray(value) && typeof value[0] === 'string') return value[0];
  return null;
}

function applyRoutePreviewQuery() {
  const layerParam = getFirstQueryString(route.query.layer)?.trim();
  if (layerParam) {
    selectedLayers.value = [layerParam];
  }

  const typeParam = getFirstQueryString(route.query.type)?.toUpperCase();
  if (typeParam === 'WMTS') {
    serviceType.value = 'WMTS';
  } else if (typeParam === 'WMS') {
    serviceType.value = 'WMS';
  }
}

onMounted(async () => {
  try {
    loading.value = true;
    const response = await layerApi.getAll();
    const summaries = response?.data?.layers || [];
    const detailPromises = summaries.map(async summary => {
      try {
        const detail = await layerApi.getByName(summary.name);
        return (detail?.data as any)?.layer;
      } catch {
        return null;
      }
    });

    const results = await Promise.all(detailPromises);
    availableLayers.value = results.filter((layer): layer is Layer => Boolean(layer));
  } catch {
    // Keep empty
  } finally {
    loading.value = false;
  }
});

async function parseWmtsCapabilities(layerName: string) {
  try {
    const capsUrl = buildCapabilitiesUrl(layerName, wmtsBaseUrl.value);
    const res = await fetch(capsUrl);
    if (!res.ok) throw new Error(`WMTS capabilities request failed: ${res.status}`);

    const xmlText = await res.text();
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlText, 'text/xml');
    if (xmlDoc.querySelector('parsererror')) throw new Error('Invalid XML response');

    const layerNode = findLayerNode(xmlDoc, layerName);
    const tileMatrixSetLink = firstDescendantByLocalName(layerNode ?? xmlDoc, 'TileMatrixSetLink');
    const tileMatrixSetElem = firstDescendantByLocalName(tileMatrixSetLink, 'TileMatrixSet');
    const parsedTmsId = tileMatrixSetElem?.textContent?.trim();

    if (parsedTmsId) {
      wmtsTileMatrixSet.value = parsedTmsId;
    }

    parsedTileMatrixSet.value = parseTileMatrixSet(xmlDoc, wmtsTileMatrixSet.value);

    const bboxNode = firstDescendantByLocalName(layerNode ?? xmlDoc, 'WGS84BoundingBox');
    if (!bboxNode) {
      parsedBounds.value = null;
      return;
    }

    const lowerCorner = firstDescendantByLocalName(bboxNode, 'LowerCorner')?.textContent;
    const upperCorner = firstDescendantByLocalName(bboxNode, 'UpperCorner')?.textContent;
    if (!lowerCorner || !upperCorner) return;

    const [minx, miny] = lowerCorner.split(/\s+/).map(Number);
    const [maxx, maxy] = upperCorner.split(/\s+/).map(Number);

    if (Number.isNaN(minx) || Number.isNaN(miny) || Number.isNaN(maxx) || Number.isNaN(maxy)) {
      return;
    }

    parsedBounds.value = [
      [miny, minx],
      [maxy, maxx]
    ];
  } catch {
    parsedTileMatrixSet.value = null;
    parsedBounds.value = null;
  }
}

watch(
  () => [route.query.layer, route.query.type],
  () => {
    applyRoutePreviewQuery();
  },
  { immediate: true }
);

// 监听图层选择变化，自动切换服务类型
watch(selectedLayers, (layers, prevLayers) => {
  // 只在选择新图层时自动切换，避免影响 URL 参数和手动切换
  if (layers.length === 1 && layers[0] !== prevLayers?.[0]) {
    const inferredType = inferServiceTypeFromLayer(layers[0]);
    if (inferredType && inferredType !== serviceType.value) {
      serviceType.value = inferredType;
    }
  }
});

watch(
  [selectedLayers, serviceType],
  ([layers, type]) => {
    if (type !== 'WMTS' || layers.length !== 1) {
      parsedTileMatrixSet.value = null;
      parsedBounds.value = null;
      return;
    }

    parseWmtsCapabilities(layers[0]);
  },
  { immediate: true }
);

function firstDescendantByLocalName(root: ParentNode | null | undefined, localName: string): Element | null {
  if (!root) return null;
  const elements = (root as Document | Element).getElementsByTagNameNS('*', localName);
  return elements.length > 0 ? elements[0] : null;
}

function directChildByLocalName(root: Element, localName: string): Element | null {
  for (const child of Array.from(root.children)) {
    if (child.localName === localName) {
      return child;
    }
  }
  return null;
}

function findLayerNode(xmlDoc: Document, qualifiedLayerName: string): Element | null {
  const layers = Array.from(xmlDoc.getElementsByTagNameNS('*', 'Layer'));
  const normalized = qualifiedLayerName.trim();
  if (!normalized) return null;

  return (
    layers.find(layer => {
      const identifierElem = directChildByLocalName(layer, 'Identifier');
      const identifier = identifierElem?.textContent?.trim();
      return identifier === normalized;
    }) || null
  );
}

function parseTileMatrixSet(xmlDoc: Document, tileMatrixSetId: string): TileMatrixSetInfo | null {
  const tileMatrixSets = Array.from(xmlDoc.getElementsByTagNameNS('*', 'TileMatrixSet'));
  const targetTms = tileMatrixSets.find(tms => {
    const identifier = directChildByLocalName(tms, 'Identifier');
    return identifier && identifier.textContent?.trim() === tileMatrixSetId;
  });

  if (!targetTms) return null;

  const supportedCrsElem = firstDescendantByLocalName(targetTms, 'SupportedCRS');
  const supportedCRS = supportedCrsElem?.textContent || 'EPSG:3857';

  const tileMatrixElems = Array.from(targetTms.getElementsByTagNameNS('*', 'TileMatrix'));
  const tileMatrices = tileMatrixElems
    .map(tm => {
      const identifierElem = firstDescendantByLocalName(tm, 'Identifier');
      const scaleDenomElem = firstDescendantByLocalName(tm, 'ScaleDenominator');
      const topLeftCornerElem = firstDescendantByLocalName(tm, 'TopLeftCorner');
      const tileWidthElem = firstDescendantByLocalName(tm, 'TileWidth');
      const tileHeightElem = firstDescendantByLocalName(tm, 'TileHeight');
      const matrixWidthElem = firstDescendantByLocalName(tm, 'MatrixWidth');
      const matrixHeightElem = firstDescendantByLocalName(tm, 'MatrixHeight');

      if (!identifierElem || !scaleDenomElem || !topLeftCornerElem) return null;

      const topLeftParts = topLeftCornerElem.textContent?.split(/\s+/).map(Number) || [0, 0];

      return {
        identifier: identifierElem.textContent || '0',
        scaleDenominator: Number.parseFloat(scaleDenomElem.textContent || '0'),
        topLeftCorner: [topLeftParts[0] || 0, topLeftParts[1] || 0],
        tileWidth: Number.parseInt(tileWidthElem?.textContent || '256', 10),
        tileHeight: Number.parseInt(tileHeightElem?.textContent || '256', 10),
        matrixWidth: Number.parseInt(matrixWidthElem?.textContent || '1', 10),
        matrixHeight: Number.parseInt(matrixHeightElem?.textContent || '1', 10)
      };
    })
    .filter((m): m is any => m !== null);

  if (tileMatrices.length === 0) return null;

  return {
    identifier: tileMatrixSetId,
    supportedCRS,
    tileMatrices
  };
}

function formatCoordinate(value: number | null) {
  return value === null || value === undefined ? '--' : value.toFixed(6);
}

function syncMapViewStatus() {
  const center = mapRef.value?.getCenter();
  const zoom = mapRef.value?.getZoom();

  if (center) {
    statusLongitude.value = center.lng;
    statusLatitude.value = center.lat;
  }

  if (zoom !== undefined) {
    statusZoom.value = zoom;
  }
}

function handleMapMoveEnd() {
  syncMapViewStatus();
}

function handleMapMouseMove(latlng: { lng: number; lat: number }) {
  statusLongitude.value = latlng.lng;
  statusLatitude.value = latlng.lat;
}

function handleMapMouseLeave() {
  syncMapViewStatus();
}

function handleZoomToLayer() {
  if (parsedBounds.value) {
    mapRef.value?.fitBounds(parsedBounds.value);
  } else {
    mapRef.value?.fitBounds([
      [30, 110],
      [40, 125]
    ]);
  }
}

function handleRefreshLayers() {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
  }, 500);
}

function handleCopyServiceUrl() {
  if (selectedLayers.value.length === 0) return;

  let url: string;

  if (serviceType.value === 'WMS') {
    const serviceLayer = selectedLayers.value[0];
    const baseUrl = `${wmsUrl.value}/${serviceLayer}/wms`;
    const params = new URLSearchParams({
      SERVICE: 'WMS',
      VERSION: wmsVersion.value,
      REQUEST: 'GetMap',
      LAYERS: selectedLayers.value.join(','),
      FORMAT: wmsFormat.value,
      TRANSPARENT: String(wmsTransparent.value),
      CRS: 'EPSG:4326',
      BBOX: '{minx},{miny},{maxx},{maxy}',
      WIDTH: '256',
      HEIGHT: '256'
    });
    url = `${baseUrl}?${params.toString()}`;
  } else {
    url = buildWmtsTileUrl(selectedLayers.value[0], {
      baseUrl: API_ORIGIN,
      tileMatrixSet: wmtsTileMatrixSet.value,
      format: wmtsFormat.value
    });
  }

  navigator.clipboard.writeText(url);
}

function handleCopyCapabilitiesUrl() {
  if (selectedLayers.value.length === 0) return;

  let url: string;
  const serviceLayer = selectedLayers.value[0];

  if (serviceType.value === 'WMS') {
    url = `${wmsUrl.value}/${serviceLayer}/wms?SERVICE=WMS&REQUEST=GetCapabilities`;
  } else {
    url = buildCapabilitiesUrl(serviceLayer, API_ORIGIN);
  }
  navigator.clipboard.writeText(url);
}
</script>

<template>
  <div class="preview-2d">
    <NSpin :show="loading" class="viewer-spin">
      <LeafletMap
        ref="mapRef"
        :center="[35, 117]"
        :zoom="5"
        :wms-layers="wmsLayers"
        :wmts-layers="wmtsLayers"
        :basemap="basemap"
        :initial-bounds="parsedBounds"
        @moveend="handleMapMoveEnd"
        @mousemove="handleMapMouseMove"
        @mouseleave="handleMapMouseLeave"
      />
    </NSpin>

    <div class="status-bar">
      <span>级别 {{ statusZoomText }}</span>
      <span>经度 {{ statusLongitudeText }}</span>
      <span>纬度 {{ statusLatitudeText }}</span>
    </div>

    <div class="toolbar">
      <NButtonGroup vertical>
        <NTooltip placement="right" :disabled="showLayerPopover">
          <template #trigger>
            <NPopover v-model:show="showLayerPopover" trigger="click" placement="right-start" :width="300">
              <template #trigger>
                <NButton :type="showLayerPopover ? 'primary' : 'default'" quaternary>
                  <template #icon>
                    <IconLayers />
                  </template>
                </NButton>
              </template>
              <NCard size="small" :bordered="false">
                <template #header>图层控制</template>
                <NSpace vertical>
                  <NSelect
                    v-model:value="selectedLayers"
                    :options="layerOptions"
                    placeholder="选择要显示的图层"
                    multiple
                    clearable
                    max-tag-count="responsive"
                  />
                  <NButtonGroup>
                    <NButton size="small" secondary @click="handleZoomToLayer">缩放</NButton>
                    <NButton size="small" secondary @click="handleRefreshLayers">刷新</NButton>
                  </NButtonGroup>
                </NSpace>
              </NCard>
            </NPopover>
          </template>
          图层控制
        </NTooltip>

        <NTooltip placement="right" :disabled="showSettingsPopover">
          <template #trigger>
            <NPopover v-model:show="showSettingsPopover" trigger="click" placement="right-start" :width="320">
              <template #trigger>
                <NButton :type="showSettingsPopover ? 'primary' : 'default'" quaternary>
                  <template #icon>
                    <IconSettings />
                  </template>
                </NButton>
              </template>
              <NCard size="small" :bordered="false">
                <template #header>服务设置</template>
                <NSpace vertical>
                  <div>
                    <div class="mb-1 text-xs text-gray-500">服务类型</div>
                    <NRadioGroup v-model:value="serviceType" size="small">
                      <NRadioButton value="WMS">WMS</NRadioButton>
                      <NRadioButton value="WMTS">WMTS</NRadioButton>
                    </NRadioGroup>
                  </div>

                  <div>
                    <div class="mb-1 text-xs text-gray-500">底图</div>
                    <NSelect v-model:value="basemap" :options="basemapOptions" size="small" />
                  </div>

                  <NDivider class="my-2" />

                  <template v-if="serviceType === 'WMS'">
                    <NSpace align="center">
                      <span class="w-10 text-xs text-gray-500">版本</span>
                      <NSelect
                        v-model:value="wmsVersion"
                        size="small"
                        :options="[
                          { label: '1.3.0', value: '1.3.0' },
                          { label: '1.1.1', value: '1.1.1' }
                        ]"
                      />
                    </NSpace>
                    <NSpace align="center">
                      <span class="w-10 text-xs text-gray-500">格式</span>
                      <NSelect
                        v-model:value="wmsFormat"
                        size="small"
                        :options="[
                          { label: 'PNG', value: 'image/png' },
                          { label: 'JPEG', value: 'image/jpeg' }
                        ]"
                      />
                    </NSpace>
                    <NCheckbox v-model:checked="wmsTransparent">透明背景</NCheckbox>
                  </template>

                  <template v-else>
                    <NSpace align="center">
                      <span class="w-10 text-xs text-gray-500">坐标系</span>
                      <NSelect
                        v-model:value="wmtsTileMatrixSet"
                        size="small"
                        :options="[
                          { label: 'EPSG:3857', value: 'EPSG:3857' },
                          { label: 'EPSG:4326', value: 'EPSG:4326' }
                        ]"
                      />
                    </NSpace>
                    <NSpace align="center">
                      <span class="w-10 text-xs text-gray-500">格式</span>
                      <NSelect
                        v-model:value="wmtsFormat"
                        size="small"
                        :options="[
                          { label: 'PNG', value: 'png' },
                          { label: 'JPEG', value: 'jpg' }
                        ]"
                      />
                    </NSpace>
                  </template>
                </NSpace>
              </NCard>
            </NPopover>
          </template>
          服务设置
        </NTooltip>

        <NTooltip placement="right" :disabled="showInfoPopover">
          <template #trigger>
            <NPopover v-model:show="showInfoPopover" trigger="click" placement="right-start" :width="300">
              <template #trigger>
                <NButton :type="showInfoPopover ? 'primary' : 'default'" quaternary>
                  <template #icon>
                    <IconInformation />
                  </template>
                </NButton>
              </template>
              <NCard size="small" :bordered="false">
                <template #header>图层信息</template>
                <NSpace vertical>
                  <NTag :type="serviceType === 'WMS' ? 'info' : 'success'" round size="small">
                    {{ serviceType }}
                  </NTag>
                  <NSpace justify="space-between">
                    <span class="text-xs text-gray-500">当前图层</span>
                    <strong>{{ layerSummaryText }}</strong>
                  </NSpace>
                  <NSpace v-if="selectedLayers.length > 0" wrap>
                    <NTag v-for="layerName in selectedLayers" :key="layerName" size="small" round>
                      {{ layerName }}
                    </NTag>
                  </NSpace>
                  <NSpace>
                    <NButton size="small" secondary @click="handleCopyServiceUrl">复制瓦片URL</NButton>
                    <NButton size="small" secondary @click="handleCopyCapabilitiesUrl">复制Capabilities</NButton>
                  </NSpace>
                </NSpace>
              </NCard>
            </NPopover>
          </template>
          图层信息
        </NTooltip>
      </NButtonGroup>
    </div>
  </div>
</template>

<style scoped>
.preview-2d {
  position: relative;
  height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
  min-height: 620px;
  overflow: hidden;
  background: var(--layout-bg-color);
}

.viewer-spin {
  position: absolute;
  inset: 0;
}

.viewer-spin :deep(.n-spin-content) {
  height: 100%;
}

.toolbar {
  position: absolute;
  top: 220px;
  left: 24px;
  z-index: 10;
  padding: 6px;
  border-radius: 8px;
  background: rgb(255 255 255 / 85%);
  border: 1px solid rgb(0 0 0 / 10%);
  box-shadow: 0 4px 12px rgb(0 0 0 / 12%);
}

:root.dark .toolbar {
  background: rgb(30 30 30 / 85%);
  border-color: rgb(255 255 255 / 10%);
}

.toolbar :deep(.n-button) {
  color: var(--n-text-color);
}

.toolbar :deep(.n-button:hover) {
  background: var(--n-button-color-hover);
}

.status-bar {
  position: absolute;
  bottom: 10px;
  left: 50%;
  z-index: 100;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  border-radius: 999px;
  background: var(--n-card-color);
  box-shadow: 0 4px 12px rgb(0 0 0 / 10%);
  padding: 8px 16px;
  font-size: 12px;
  pointer-events: none;
}

.preview-2d :deep(.leaflet-control-coordinates),
.preview-2d :deep(.leaflet-control-zoomlevel) {
  display: none !important;
}

@media (width <= 768px) {
  .preview-2d {
    min-height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
  }

  .toolbar {
    top: 16px;
    left: 16px;
  }

  .status-bar {
    bottom: 12px;
    padding: 6px 12px;
    font-size: 11px;
  }
}
</style>
