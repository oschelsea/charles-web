<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { NButton, NCard, NCheckbox, NRadioButton, NRadioGroup, NSelect, NSpace, NSpin, NTag } from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import { type Layer, layerApi } from '@/service/api/xenon/layer';
import { buildCapabilitiesUrl, buildWmtsTileUrl } from '@/service/api/xenon/wmts';
import LeafletMap from '@/components/xenon-map/LeafletMap.vue';
import type { TileMatrixSetInfo, WmsLayerConfig, WmtsLayerConfig } from '@/components/xenon-map/LeafletMap.vue';

const route = useRoute();

const loading = ref(false);
const mapRef = ref<InstanceType<typeof LeafletMap>>();
const controlsCollapsed = ref(false);
const infoCollapsed = ref(false);

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

const basemap = ref<'osm' | 'satellite' | 'terrain' | 'blank'>('osm');
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

  return selectedLayers.value.map(layerName => ({
    name: layerName,
    url: buildWmtsTileUrl(layerName, {
      baseUrl: wmtsBaseUrl.value,
      tileMatrixSet: wmtsTileMatrixSet.value,
      format: wmtsFormat.value
    }),
    attribution: 'Xenon WMTS',
    tileMatrixSet: parsedTileMatrixSet.value || undefined
  }));
});

const layerOptions = computed<SelectOption[]>(() =>
  availableLayers.value
    .filter(layer => layer.enabled && layer.type !== 'TILES3D')
    .map(layer => {
      const qualifiedName = layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
      return {
        label: `${layer.type === 'VECTOR' ? '📐' : '🖼️'} ${layer.workspaceName ? `${layer.workspaceName}:` : ''}${layer.title || layer.name}`,
        value: qualifiedName
      };
    })
);

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

watch(
  () => [route.query.layer, route.query.type],
  () => {
    applyRoutePreviewQuery();
  },
  { immediate: true }
);

watch([selectedLayers, serviceType], async ([layers, type]) => {
  if (type !== 'WMTS' || layers.length !== 1) {
    parsedTileMatrixSet.value = null;
    parsedBounds.value = null;
    return;
  }

  const layerName = layers[0];

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

    setTimeout(() => {
      nextTick(() => {
        if (mapRef.value && parsedBounds.value) {
          mapRef.value.fitBounds(parsedBounds.value);
        }
      });
    }, 500);
  } catch {
    parsedTileMatrixSet.value = null;
    parsedBounds.value = null;
  }
});

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
      baseUrl: wmtsBaseUrl.value,
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
    url = buildCapabilitiesUrl(serviceLayer, wmtsBaseUrl.value);
  }
  navigator.clipboard.writeText(url);
}
</script>

<template>
  <div class="preview-2d">
    <NSpin :show="loading" class="viewer-spin">
      <div class="map-stage">
        <LeafletMap
          ref="mapRef"
          :center="[35, 117]"
          :zoom="5"
          :wms-layers="wmsLayers"
          :wmts-layers="wmtsLayers"
          :basemap="basemap"
          @moveend="handleMapMoveEnd"
          @mousemove="handleMapMouseMove"
          @mouseleave="handleMapMouseLeave"
        />
      </div>
    </NSpin>

    <div class="status-bar">
      <span class="status-item">级别 {{ statusZoomText }}</span>
      <span class="status-item">经度 {{ statusLongitudeText }}</span>
      <span class="status-item">纬度 {{ statusLatitudeText }}</span>
    </div>

    <div class="map-overlay">
      <div class="floating-panel floating-panel--left" :class="{ 'floating-panel--collapsed': controlsCollapsed }">
        <NButton
          v-if="controlsCollapsed"
          class="panel-handle"
          secondary
          strong
          size="small"
          round
          @click="controlsCollapsed = false"
        >
          <template #icon>
            <div class="i-mdi-tune-variant"></div>
          </template>
          工具
        </NButton>

        <NCard v-else class="floating-card control-card" size="small">
          <template #header>
            <div class="panel-heading">
              <strong>二维地图工具条</strong>
            </div>
          </template>
          <template #header-extra>
            <NButton text size="small" @click="controlsCollapsed = true">收起</NButton>
          </template>

          <div class="section-stack">
            <section class="section-block">
              <div class="section-caption">图层</div>
              <NSelect
                v-model:value="selectedLayers"
                :options="layerOptions"
                placeholder="选择要显示的图层"
                multiple
                clearable
                max-tag-count="responsive"
              />
              <div class="action-row">
                <NButton size="small" secondary block @click="handleZoomToLayer">缩放</NButton>
                <NButton size="small" secondary block @click="handleRefreshLayers">刷新</NButton>
              </div>
            </section>

            <section class="section-block">
              <div class="section-caption">服务</div>
              <NRadioGroup v-model:value="serviceType" size="small" class="service-switch">
                <NRadioButton value="WMS">WMS</NRadioButton>
                <NRadioButton value="WMTS">WMTS</NRadioButton>
              </NRadioGroup>
            </section>

            <section class="section-block">
              <div class="section-caption">底图</div>
              <NSelect v-model:value="basemap" :options="basemapOptions" size="small" />
            </section>

            <section class="section-block">
              <div class="section-caption">参数</div>

              <template v-if="serviceType === 'WMS'">
                <div class="param-row">
                  <span class="param-label">版本</span>
                  <NSelect
                    v-model:value="wmsVersion"
                    size="small"
                    :options="[
                      { label: '1.3.0', value: '1.3.0' },
                      { label: '1.1.1', value: '1.1.1' }
                    ]"
                  />
                </div>
                <div class="param-row">
                  <span class="param-label">格式</span>
                  <NSelect
                    v-model:value="wmsFormat"
                    size="small"
                    :options="[
                      { label: 'PNG', value: 'image/png' },
                      { label: 'JPEG', value: 'image/jpeg' },
                      { label: 'GIF', value: 'image/gif' }
                    ]"
                  />
                </div>
                <NCheckbox v-model:checked="wmsTransparent">透明背景</NCheckbox>
              </template>

              <template v-else>
                <div class="param-row">
                  <span class="param-label">坐标系</span>
                  <NSelect
                    v-model:value="wmtsTileMatrixSet"
                    size="small"
                    :options="[
                      { label: 'Web Mercator (EPSG:3857)', value: 'EPSG:3857' },
                      { label: 'WGS84 (EPSG:4326)', value: 'EPSG:4326' }
                    ]"
                  />
                </div>
                <div class="param-row">
                  <span class="param-label">格式</span>
                  <NSelect
                    v-model:value="wmtsFormat"
                    size="small"
                    :options="[
                      { label: 'PNG', value: 'png' },
                      { label: 'JPEG', value: 'jpg' }
                    ]"
                  />
                </div>
              </template>
            </section>
          </div>
        </NCard>
      </div>

      <div class="floating-panel floating-panel--right" :class="{ 'floating-panel--collapsed': infoCollapsed }">
        <NButton
          v-if="infoCollapsed"
          class="panel-handle"
          secondary
          strong
          size="small"
          round
          @click="infoCollapsed = false"
        >
          <template #icon>
            <div class="i-mdi-information-outline"></div>
          </template>
          信息
        </NButton>

        <NCard v-else class="floating-card info-card" size="small">
          <template #header>
            <div class="panel-heading">
              <strong>图层信息</strong>
            </div>
          </template>
          <template #header-extra>
            <NButton text size="small" @click="infoCollapsed = true">收起</NButton>
          </template>

          <NSpace vertical size="small">
            <NTag :type="serviceType === 'WMS' ? 'info' : 'success'" round size="small">{{ serviceType }}</NTag>
            <div class="info-row">
              <span class="info-label">当前图层</span>
              <strong class="info-value">{{ layerSummaryText }}</strong>
            </div>
            <div class="layer-tag-list">
              <NTag v-for="layerName in selectedLayers" :key="layerName" size="small" round>
                {{ layerName }}
              </NTag>
            </div>
            <NSpace>
              <NButton size="small" secondary @click="handleCopyServiceUrl">复制瓦片URL</NButton>
              <NButton size="small" secondary @click="handleCopyCapabilitiesUrl">复制Capabilities URL</NButton>
            </NSpace>
          </NSpace>
        </NCard>
      </div>
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
  isolation: isolate;
}

.viewer-spin,
.viewer-spin :deep(.n-spin-content) {
  position: absolute;
  inset: 0;
  height: 100%;
  width: 100%;
  z-index: 0;
}

.map-stage {
  min-height: 100%;
  height: 100%;
  width: 100%;
}

.status-bar {
  position: absolute;
  bottom: 10px;
  left: 50%;
  z-index: 4;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
  max-width: calc(100% - 40px);
  border: 1px solid var(--n-border-color);
  border-radius: 999px;
  background: var(--n-card-color);
  box-shadow: 0 8px 20px rgb(15 23 42 / 10%);
  padding: 8px 14px;
  color: #111827;
  font-size: 12px;
  line-height: 1.4;
  pointer-events: none;
}

.status-item {
  white-space: nowrap;
  color: #111827;
}

.map-overlay {
  position: absolute;
  inset: 0;
  z-index: 3;
  padding: 24px;
  pointer-events: none;
}

.floating-panel {
  position: absolute;
  pointer-events: auto;
}

.floating-panel--left {
  top: 124px;
  left: 24px;
  width: min(308px, calc(100% - 48px));
}

.floating-panel--right {
  top: 78px;
  right: 24px;
  width: min(292px, calc(100% - 48px));
}

.floating-panel--left.floating-panel--collapsed {
  top: 120px;
  left: 16px;
  width: auto;
}

.floating-panel--right.floating-panel--collapsed {
  top: 120px;
  right: 16px;
  width: auto;
}

.floating-card {
  box-shadow: 0 12px 28px rgb(15 23 42 / 10%);
}

.panel-handle {
  box-shadow: 0 8px 18px rgb(15 23 42 / 10%);
}

.control-card,
.info-card {
  width: 100%;
}

.section-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-caption {
  font-size: 12px;
  font-weight: 600;
  color: inherit;
}

.service-switch {
  width: 100%;
}

.action-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.panel-heading strong {
  font-size: 14px;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param-label {
  width: 44px;
  flex-shrink: 0;
  font-size: 13px;
  color: inherit;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.info-label {
  font-size: 12px;
  color: inherit;
}

.info-value {
  text-align: right;
  color: var(--n-text-color);
}

.layer-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-2d :deep(.leaflet-control-coordinates),
.preview-2d :deep(.leaflet-control-zoomlevel) {
  display: none !important;
}

@media (width <= 768px) {
  .preview-2d {
    min-height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
    height: auto;
  }

  .map-overlay {
    padding: 16px;
  }

  .status-bar {
    bottom: 12px;
    width: calc(100% - 32px);
    max-width: none;
    gap: 10px;
    justify-content: center;
    padding: 8px 12px;
  }

  .floating-panel--left,
  .floating-panel--right {
    right: 16px;
    left: 16px;
    width: auto;
    max-width: none;
  }

  .floating-panel--right {
    top: auto;
    bottom: 20px;
  }

  .floating-panel--left.floating-panel--collapsed,
  .floating-panel--right.floating-panel--collapsed {
    top: auto;
    bottom: 74px;
    width: auto;
  }

  .floating-panel--left.floating-panel--collapsed {
    left: 16px;
    right: auto;
  }

  .floating-panel--right.floating-panel--collapsed {
    right: 16px;
    left: auto;
  }
}
</style>
