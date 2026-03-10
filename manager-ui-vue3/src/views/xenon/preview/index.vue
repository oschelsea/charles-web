<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import {
  NCard,
  NSpace,
  NSelect,
  NButton,
  NCheckbox,
  NCollapse,
  NCollapseItem,
  NTag,
  NSpin,
  NRadioGroup,
  NRadioButton
} from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import LeafletMap from '@/components/xenon-map/LeafletMap.vue';
import type { WmsLayerConfig, WmtsLayerConfig, TileMatrixSetInfo } from '@/components/xenon-map/LeafletMap.vue';

import { layerApi, type Layer } from '@/service/api/xenon/layer';
import { buildWmtsTileUrl, buildCapabilitiesUrl } from '@/service/api/xenon/wmts';

const route = useRoute();

const loading = ref(false);
const mapRef = ref<InstanceType<typeof LeafletMap>>();

const availableLayers = ref<Layer[]>([]);
const serviceType = ref<'WMS' | 'WMTS'>('WMS');
const selectedLayers = ref<string[]>([]);

// Since this is proxied by Vite or Nginx to the backend, dev-api is usually the prefix.
// We'll calculate base URL using the window location properties for a robust client-side approach.
const API_BASE_URL = `${window.location.protocol}//${window.location.host}${import.meta.env.VITE_SERVICE_BASE_URL}`;
const CONTEXT_PATH = '/xenon';

const wmsUrl = ref(`${API_BASE_URL}${CONTEXT_PATH}/services`);
const wmsVersion = ref('1.3.0');
const wmsFormat = ref('image/png');
const wmsTransparent = ref(true);

const wmtsBaseUrl = ref(API_BASE_URL);
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
    .filter(l => l.enabled && l.type !== 'TILES3D')
    .map(layer => {
      const qualifiedName = layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
      return {
        label: `${layer.type === 'VECTOR' ? '📐' : '🖼️'} ${layer.workspaceName ? layer.workspaceName + ':' : ''}${layer.title || layer.name}`,
        value: qualifiedName
      };
    })
);

onMounted(async () => {
  try {
    loading.value = true;
    const response = await layerApi.getAll();
    const fullLayers: Layer[] = [];
    for (const summary of response?.data?.layers || []) {
      try {
        const detail = await layerApi.getByName(summary.name);
        const layerData = (detail?.data as any)?.layer;
        if (layerData) {
          fullLayers.push(layerData);
        }
      } catch {
        // Skip
      }
    }
    availableLayers.value = fullLayers;
  } catch {
    // Keep empty
  } finally {
    loading.value = false;
  }

  const layerParam = route.query.layer as string;
  if (layerParam) {
    selectedLayers.value = [layerParam];
  }
  
  const typeParam = route.query.type as string;
  if (typeParam?.toUpperCase() === 'WMTS') {
    serviceType.value = 'WMTS';
  }
});

watch([selectedLayers, serviceType], async ([layers, type]) => {
  if (type === 'WMTS' && layers.length === 1) {
    const layerName = layers[0];
    try {
      const capsUrl = buildCapabilitiesUrl(layerName, wmtsBaseUrl.value);
      const res = await fetch(capsUrl);
      const xmlText = await res.text();
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlText, 'text/xml');
      
      const tmsLink = xmlDoc.querySelector('TileMatrixSetLink TileMatrixSet');
      if (tmsLink?.textContent) {
        wmtsTileMatrixSet.value = tmsLink.textContent;
        parsedTileMatrixSet.value = parseTileMatrixSet(xmlDoc, tmsLink.textContent);
      }
      
      const wgs84Bbox = xmlDoc.querySelector('WGS84BoundingBox');
      if (wgs84Bbox) {
         const lowerCorner = wgs84Bbox.querySelector('LowerCorner')?.textContent;
         const upperCorner = wgs84Bbox.querySelector('UpperCorner')?.textContent;
         if (lowerCorner && upperCorner) {
            const [minx, miny] = lowerCorner.split(/\s+/).map(Number);
            const [maxx, maxy] = upperCorner.split(/\s+/).map(Number);
            if (!isNaN(minx) && !isNaN(miny) && !isNaN(maxx) && !isNaN(maxy)) {
               parsedBounds.value = [[miny, minx], [maxy, maxx]];
               setTimeout(() => {
                 nextTick(() => {
                   if (mapRef.value && parsedBounds.value) {
                     mapRef.value.fitBounds(parsedBounds.value);
                   }
                 });
               }, 500);
            }
         }
      }
    } catch {
      parsedTileMatrixSet.value = null;
      parsedBounds.value = null;
    }
  } else {
    parsedTileMatrixSet.value = null;
    parsedBounds.value = null;
  }
});

function parseTileMatrixSet(xmlDoc: Document, tileMatrixSetId: string): TileMatrixSetInfo | null {
  const tileMatrixSets = Array.from(xmlDoc.querySelectorAll('TileMatrixSet'));
  let targetTms: Element | null = null;
  
  for (const tms of tileMatrixSets) {
    const identifier = tms.querySelector(':scope > Identifier, :scope > ows\\:Identifier');
    if (identifier && identifier.textContent === tileMatrixSetId) {
      targetTms = tms;
      break;
    }
  }
  
  if (!targetTms) return null;
  
  const supportedCrsElem = targetTms.querySelector('SupportedCRS, ows\\:SupportedCRS');
  const supportedCRS = supportedCrsElem?.textContent || 'EPSG:3857';
  
  // Some environments don't support NodeList iteration via for...of correctly in TS
  // so we convert it to Array
  const tileMatrixElems = Array.from(targetTms.querySelectorAll('TileMatrix'));
  const tileMatrices: any[] = [];
  
  for (const tm of tileMatrixElems) {
    const identifierElem = tm.querySelector('Identifier, ows\\:Identifier');
    const scaleDenomElem = tm.querySelector('ScaleDenominator');
    const topLeftCornerElem = tm.querySelector('TopLeftCorner');
    const tileWidthElem = tm.querySelector('TileWidth');
    const tileHeightElem = tm.querySelector('TileHeight');
    const matrixWidthElem = tm.querySelector('MatrixWidth');
    const matrixHeightElem = tm.querySelector('MatrixHeight');
    
    if (!identifierElem || !scaleDenomElem || !topLeftCornerElem) continue;
    
    const topLeftParts = topLeftCornerElem.textContent?.split(/\s+/).map(Number) || [0, 0];
    
    tileMatrices.push({
      identifier: identifierElem.textContent || '0',
      scaleDenominator: parseFloat(scaleDenomElem.textContent || '0'),
      topLeftCorner: [topLeftParts[0] || 0, topLeftParts[1] || 0],
      tileWidth: parseInt(tileWidthElem?.textContent || '256'),
      tileHeight: parseInt(tileHeightElem?.textContent || '256'),
      matrixWidth: parseInt(matrixWidthElem?.textContent || '1'),
      matrixHeight: parseInt(matrixHeightElem?.textContent || '1')
    });
  }
  
  if (tileMatrices.length === 0) return null;
  
  return {
    identifier: tileMatrixSetId,
    supportedCRS,
    tileMatrices
  };
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
  <div class="p-4 h-full">
    <div class="flex h-full gap-4 map-preview">
      <div class="w-80 flex-shrink-0 overflow-y-auto">
        <NCard title="图层控制" :bordered="false" size="small">
          <NSpin :show="loading">
            <NCollapse default-expanded-names="layers" arrow-placement="right">
              <NCollapseItem title="服务类型" name="service">
                <NRadioGroup v-model:value="serviceType" size="small">
                  <NRadioButton value="WMS">WMS</NRadioButton>
                  <NRadioButton value="WMTS">WMTS</NRadioButton>
                </NRadioGroup>
              </NCollapseItem>

              <NCollapseItem title="图层选择" name="layers">
                <NSpace vertical>
                  <NSelect
                    v-model:value="selectedLayers"
                    :options="layerOptions"
                    placeholder="选择要显示的图层"
                    multiple
                    clearable
                    max-tag-count="responsive"
                  />
                  <NSpace>
                    <NButton size="small" @click="handleZoomToLayer">
                      缩放到图层
                    </NButton>
                    <NButton size="small" @click="handleRefreshLayers">
                      刷新
                    </NButton>
                  </NSpace>
                </NSpace>
              </NCollapseItem>

              <NCollapseItem v-if="serviceType === 'WMS'" title="WMS参数" name="wms">
                <NSpace vertical size="small">
                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">版本:</span>
                    <NSelect
                      v-model:value="wmsVersion"
                      size="small"
                      :options="[
                        { label: '1.3.0', value: '1.3.0' },
                        { label: '1.1.1', value: '1.1.1' }
                      ]"
                      style="width: 100px"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">格式:</span>
                    <NSelect
                      v-model:value="wmsFormat"
                      size="small"
                      :options="[
                        { label: 'PNG', value: 'image/png' },
                        { label: 'JPEG', value: 'image/jpeg' },
                        { label: 'GIF', value: 'image/gif' }
                      ]"
                      style="width: 100px"
                    />
                  </div>
                  <NCheckbox v-model:checked="wmsTransparent">
                    透明背景
                  </NCheckbox>
                </NSpace>
              </NCollapseItem>

              <NCollapseItem v-if="serviceType === 'WMTS'" title="WMTS参数" name="wmts">
                <NSpace vertical size="small">
                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">坐标系:</span>
                    <NSelect
                      v-model:value="wmtsTileMatrixSet"
                      size="small"
                      :options="[
                        { label: 'Web Mercator (EPSG:3857)', value: 'EPSG:3857' },
                        { label: 'WGS84 (EPSG:4326)', value: 'EPSG:4326' }
                      ]"
                      style="width: 180px"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">格式:</span>
                    <NSelect
                      v-model:value="wmtsFormat"
                      size="small"
                      :options="[
                        { label: 'PNG', value: 'png' },
                        { label: 'JPEG', value: 'jpg' }
                      ]"
                      style="width: 100px"
                    />
                  </div>
                </NSpace>
              </NCollapseItem>

              <NCollapseItem title="底图" name="basemap">
                <NSelect
                  v-model:value="basemap"
                  :options="basemapOptions"
                  size="small"
                />
              </NCollapseItem>
            </NCollapse>
          </NSpin>
        </NCard>

        <NCard title="图层信息" :bordered="false" size="small" class="mt-4">
          <div v-if="selectedLayers.length > 0">
            <NSpace vertical size="small">
              <div class="mb-2">
                <NTag :type="serviceType === 'WMS' ? 'info' : 'success'" round size="small">
                  {{ serviceType }}
                </NTag>
              </div>
              <div v-for="layerName in selectedLayers" :key="layerName">
                <NTag type="info" round>{{ layerName }}</NTag>
              </div>
              <NSpace class="mt-2">
                <NButton 
                  size="small" 
                  tertiary 
                  @click="handleCopyServiceUrl"
                >
                  复制瓦片URL
                </NButton>
                <NButton 
                  size="small" 
                  tertiary 
                  @click="handleCopyCapabilitiesUrl"
                >
                  复制Capabilities URL
                </NButton>
              </NSpace>
            </NSpace>
          </div>
          <div v-else class="text-center p-4 text-[13px] text-[var(--n-text-color-3)]">
            选择图层以查看信息
          </div>
        </NCard>
      </div>

      <div class="flex-1 rounded-xl overflow-hidden shadow-sm border border-gray-200 dark:border-gray-800">
        <LeafletMap
          ref="mapRef"
          :center="[35, 117]"
          :zoom="5"
          :wms-layers="wmsLayers"
          :wmts-layers="wmtsLayers"
          :basemap="basemap"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.map-preview {
  height: calc(100vh - 120px);
}
</style>
