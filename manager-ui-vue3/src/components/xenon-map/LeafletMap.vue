<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import * as L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import proj4 from 'proj4';
import 'proj4leaflet';

export interface WmsLayerConfig {
  name: string;
  url: string;
  layers: string;
  transparent?: boolean;
  format?: string;
  version?: string;
  styles?: string;
  crs?: string;
}

/**
 * TileMatrix 信息，从 WMTS Capabilities 解析
 */
export interface TileMatrixInfo {
  identifier: string | number;
  scaleDenominator: number;
  resolution?: number;
  topLeftCorner: [number, number];
  tileWidth: number;
  tileHeight: number;
  matrixWidth: number;
  matrixHeight: number;
}

/**
 * TileMatrixSet 信息
 */
export interface TileMatrixSetInfo {
  identifier: string;
  supportedCRS: string;
  tileMatrices: TileMatrixInfo[];
}

export interface WmtsLayerConfig {
  name: string;
  url: string;
  attribution?: string;
  tileMatrixSet?: TileMatrixSetInfo;
}

interface Props {
  center?: [number, number];
  zoom?: number;
  wmsLayers?: WmsLayerConfig[];
  wmtsLayers?: WmtsLayerConfig[];
  basemap?: 'osm' | 'satellite' | 'terrain' | 'blank';
  initialBounds?: [[number, number], [number, number]] | null;
}

const props = withDefaults(defineProps<Props>(), {
  center: () => [35, 117],
  zoom: 5,
  wmsLayers: () => [],
  wmtsLayers: () => [],
  basemap: 'satellite',
  initialBounds: null
});

const emit = defineEmits<{
  (e: 'click', latlng: L.LatLng): void;
  (e: 'moveend', bounds: L.LatLngBounds): void;
  (e: 'mousemove', latlng: L.LatLng): void;
  (e: 'mouseleave'): void;
}>();

const mapContainer = ref<HTMLDivElement>();
let map: L.Map | null = null;
let basemapLayer: L.TileLayer | null = null;
let wmsLayerGroup: L.LayerGroup | null = null;
let wmtsLayerGroup: L.LayerGroup | null = null;

const customCrsCache = new Map<string, L.CRS>();

const basemaps: Record<string, { url: string; attribution: string; options?: L.TileLayerOptions }> = {
  osm: {
    url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '© OpenStreetMap contributors'
  },
  satellite: {
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
    attribution: '© Esri'
  },
  terrain: {
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
    attribution: '© Esri'
  },
  blank: {
    url: '',
    attribution: ''
  }
};

const METERS_PER_UNIT: Record<string, number> = {
  m: 1,
  meter: 1,
  meters: 1,
  degree: 111319.49079327358,
  degrees: 111319.49079327358,
  ft: 0.3048,
  feet: 0.3048
};

function scaleToResolution(scaleDenominator: number, crsCode: string): number {
  const epsgCode = extractEpsgCode(crsCode);
  const isGeographic = epsgCode === '4326' || epsgCode === '4610' || epsgCode === '4214' || epsgCode === '4490';
  const metersPerUnit = isGeographic ? METERS_PER_UNIT.degree : METERS_PER_UNIT.m;
  return (scaleDenominator * 0.00028) / metersPerUnit;
}

function extractEpsgCode(crs: string): string {
  const match = crs.match(/EPSG[^0-9]*(\d+)/i);
  return match ? match[1] : '';
}

function getProj4Def(epsgCode: string): string {
  const proj4Defs: Record<string, string> = {
    '4326': '+proj=longlat +datum=WGS84 +no_defs',
    '3857':
      '+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext +no_defs',
    '4214': '+proj=longlat +ellps=krass +no_defs',
    '4490': '+proj=longlat +ellps=GRS80 +no_defs',
    '4610': '+proj=longlat +ellps=clrk80ign +no_defs'
  };
  return proj4Defs[epsgCode] || '+proj=longlat +ellps=WGS84 +no_defs';
}

function createCustomCrs(tileMatrixSet: TileMatrixSetInfo): L.CRS {
  const cacheKey = tileMatrixSet.identifier;
  if (customCrsCache.has(cacheKey)) {
    return customCrsCache.get(cacheKey)!;
  }

  const epsgCode = extractEpsgCode(tileMatrixSet.supportedCRS);
  const proj4Def = getProj4Def(epsgCode);

  if (epsgCode && !proj4.defs(`EPSG:${epsgCode}`)) {
    proj4.defs(`EPSG:${epsgCode}`, proj4Def);
  }

  const sortedMatrices = [...tileMatrixSet.tileMatrices].sort((a, b) => {
    const aLevel = typeof a.identifier === 'number' ? a.identifier : Number.parseInt(String(a.identifier), 10);
    const bLevel = typeof b.identifier === 'number' ? b.identifier : Number.parseInt(String(b.identifier), 10);
    return aLevel - bLevel;
  });

  const resolutions: number[] = sortedMatrices.map(tm => {
    if (tm.resolution !== undefined) {
      return tm.resolution;
    }
    return scaleToResolution(tm.scaleDenominator, tileMatrixSet.supportedCRS);
  });

  const origin = sortedMatrices[0]?.topLeftCorner || [0, 0];
  const isGeographic = epsgCode === '4326' || epsgCode === '4214' || epsgCode === '4490';

  const crs = new (L as any).Proj.CRS(`EPSG:${epsgCode}`, proj4Def, {
    resolutions,
    origin: isGeographic ? [origin[1], origin[0]] : origin
  });

  // 确保 CRS 对象有 code 属性，用于后续比较
  (crs as any).code = `EPSG:${epsgCode}`;

  customCrsCache.set(cacheKey, crs);
  return crs;
}

function addCoordinatesControl(mapInstance: L.Map) {
  const CoordinatesControl = L.Control.extend({
    onAdd() {
      const container = L.DomUtil.create('div', 'leaflet-control-coordinates');
      container.style.cssText = `
        background: rgba(30, 30, 40, 0.9);
        padding: 4px 8px;
        border-radius: 4px;
        color: #fff;
        font-size: 12px;
        font-family: monospace;
        min-width: 180px;
      `;
      container.innerHTML = '经度: -- 纬度: --';
      return container;
    }
  });

  const coordControl = new (CoordinatesControl as unknown as typeof L.Control)({ position: 'bottomright' });
  coordControl.addTo(mapInstance);

  mapInstance.on('mousemove', (e: L.LeafletMouseEvent) => {
    const container = document.querySelector('.leaflet-control-coordinates');
    if (container) {
      const lng = e.latlng.lng.toFixed(6);
      const lat = e.latlng.lat.toFixed(6);
      container.innerHTML = `经度: ${lng} 纬度: ${lat}`;
    }
  });

  mapInstance.on('mouseout', () => {
    const container = document.querySelector('.leaflet-control-coordinates');
    if (container) {
      container.innerHTML = '经度: -- 纬度: --';
    }
  });
}

function addZoomLevelControl(mapInstance: L.Map) {
  const ZoomLevelControl = L.Control.extend({
    onAdd() {
      const container = L.DomUtil.create('div', 'leaflet-control-zoomlevel');
      container.style.cssText = `
        background: rgba(30, 30, 40, 0.9);
        padding: 4px 8px;
        border-radius: 4px;
        color: #fff;
        font-size: 12px;
        font-family: monospace;
      `;
      container.innerHTML = `级别: ${mapInstance.getZoom()}`;
      return container;
    }
  });

  const zoomControl = new (ZoomLevelControl as unknown as typeof L.Control)({ position: 'topright' });
  zoomControl.addTo(mapInstance);

  mapInstance.on('zoomend', () => {
    const container = document.querySelector('.leaflet-control-zoomlevel');
    if (container) {
      container.innerHTML = `级别: ${mapInstance.getZoom()}`;
    }
  });
}

onMounted(() => {
  nextTick(() => {
    initMap();
  });
});

onUnmounted(() => {
  if (map) {
    map.remove();
    map = null;
  }
});

watch(
  () => props.basemap,
  newBasemap => {
    updateBasemap(newBasemap);
  }
);

watch(
  () => props.wmsLayers,
  newLayers => {
    updateWmsLayers(newLayers);
  },
  { deep: true }
);

watch(
  () => props.wmtsLayers,
  newLayers => {
    updateWmtsLayers(newLayers);
  },
  { deep: true }
);

function initMap() {
  if (!mapContainer.value) return;

  map = L.map(mapContainer.value, {
    center: props.center,
    zoom: props.zoom,
    zoomControl: true,
    attributionControl: true
  });

  updateBasemap(props.basemap);

  wmsLayerGroup = L.layerGroup().addTo(map);
  wmtsLayerGroup = L.layerGroup().addTo(map);

  if (props.wmsLayers.length > 0) {
    updateWmsLayers(props.wmsLayers);
  }

  if (props.wmtsLayers.length > 0) {
    updateWmtsLayers(props.wmtsLayers);
  }

  L.control
    .scale({
      position: 'bottomleft',
      metric: true,
      imperial: false
    })
    .addTo(map);

  addCoordinatesControl(map);
  addZoomLevelControl(map);

  map.on('click', e => {
    emit('click', e.latlng);
  });

  map.on('mousemove', e => {
    emit('mousemove', e.latlng);
  });

  map.on('mouseout', () => {
    emit('mouseleave');
  });

  map.on('moveend', () => {
    if (map) {
      emit('moveend', map.getBounds());
    }
  });
}

function updateBasemap(basemapId: string) {
  if (!map) return;

  if (basemapLayer) {
    map.removeLayer(basemapLayer);
    basemapLayer = null;
  }

  const config = basemaps[basemapId];
  if (config && config.url) {
    basemapLayer = L.tileLayer(config.url, {
      attribution: config.attribution,
      maxZoom: 19,
      ...config.options
    }).addTo(map);
  }
}

function updateWmsLayers(layers: WmsLayerConfig[]) {
  if (!map || !wmsLayerGroup) return;

  wmsLayerGroup.clearLayers();

  layers.forEach(config => {
    const wmsLayer = L.tileLayer.wms(config.url, {
      layers: config.layers,
      format: config.format || 'image/png',
      transparent: config.transparent !== false,
      version: config.version || '1.3.0',
      styles: config.styles || '',
      crs: config.crs === 'EPSG:4326' ? L.CRS.EPSG4326 : L.CRS.EPSG3857
    } as L.WMSOptions);

    wmsLayerGroup!.addLayer(wmsLayer);
  });
}

function updateWmtsLayers(layers: WmtsLayerConfig[]) {
  if (!map || !wmtsLayerGroup) return;

  wmtsLayerGroup.clearLayers();

  layers.forEach(config => {
    if (config.tileMatrixSet && config.tileMatrixSet.tileMatrices.length > 0) {
      addCustomCrsWmtsLayer(config);
    } else {
      const wmtsLayer = L.tileLayer(config.url, {
        attribution: config.attribution || '',
        maxZoom: 18,
        tms: false
      });

      wmtsLayerGroup!.addLayer(wmtsLayer);
    }
  });
}

function addCustomCrsWmtsLayer(config: WmtsLayerConfig) {
  if (!map || !wmtsLayerGroup || !config.tileMatrixSet) return;

  const tileMatrixSet = config.tileMatrixSet;
  const crs = createCustomCrs(tileMatrixSet);

  const sortedMatrices = [...tileMatrixSet.tileMatrices].sort((a, b) => {
    const aLevel = typeof a.identifier === 'number' ? a.identifier : Number.parseInt(String(a.identifier), 10);
    const bLevel = typeof b.identifier === 'number' ? b.identifier : Number.parseInt(String(b.identifier), 10);
    return aLevel - bLevel;
  });

  const minZoom = 0;
  const maxZoom = sortedMatrices.length - 1;
  const tileSize = sortedMatrices[0]?.tileWidth || 256;

  const CustomTileLayer = L.TileLayer.extend({
    getTileUrl(coords: L.Coords) {
      const z = coords.z;
      const x = coords.x;
      const y = coords.y;

      return config.url.replace('{z}', String(z)).replace('{x}', String(x)).replace('{y}', String(y));
    }
  });

  const currentCrsCode = (map.options.crs as any)?.code || 'EPSG:3857';
  const targetCrsCode = `EPSG:${extractEpsgCode(tileMatrixSet.supportedCRS)}`;

  if (currentCrsCode !== targetCrsCode) {
    reinitMapWithCrs({ crs, config, minZoom, maxZoom, tileSize });
  } else {
    const wmtsLayer = new (CustomTileLayer as unknown as typeof L.TileLayer)(config.url, {
      attribution: config.attribution || '',
      minZoom,
      maxZoom,
      tileSize,
      tms: false
    });

    wmtsLayerGroup!.addLayer(wmtsLayer);
  }
}

function reinitMapWithCrs(options: {
  crs: L.CRS;
  config: WmtsLayerConfig;
  minZoom: number;
  maxZoom: number;
  tileSize: number;
}) {
  const { crs, config, minZoom, maxZoom, tileSize } = options;

  if (!mapContainer.value || !config.tileMatrixSet) return;

  const currentCenter = map?.getCenter() || L.latLng(props.center![0], props.center![1]);
  const currentZoom = map?.getZoom() || props.zoom;

  if (map) {
    map.remove();
    map = null;
  }

  map = L.map(mapContainer.value, {
    crs,
    center: currentCenter,
    zoom: Math.min(currentZoom!, maxZoom),
    minZoom,
    maxZoom,
    zoomControl: true,
    attributionControl: true
  });

  // 添加底图
  updateBasemap(props.basemap);

  wmsLayerGroup = L.layerGroup().addTo(map);
  wmtsLayerGroup = L.layerGroup().addTo(map);

  const CustomTileLayer = L.TileLayer.extend({
    getTileUrl(coords: L.Coords) {
      const z = coords.z;
      const x = coords.x;
      const y = coords.y;

      return config.url.replace('{z}', String(z)).replace('{x}', String(x)).replace('{y}', String(y));
    }
  });

  const wmtsLayer = new (CustomTileLayer as unknown as typeof L.TileLayer)(config.url, {
    attribution: config.attribution || '',
    minZoom,
    maxZoom,
    tileSize,
    tms: false
  });

  wmtsLayerGroup.addLayer(wmtsLayer);

  // 如果有初始范围，定位到该范围（延迟执行以确保图层已渲染）
  if (props.initialBounds) {
    setTimeout(() => {
      map?.fitBounds(props.initialBounds!);
    }, 100);
  }

  L.control
    .scale({
      position: 'bottomleft',
      metric: true,
      imperial: false
    })
    .addTo(map);

  addCoordinatesControl(map);
  addZoomLevelControl(map);

  map.on('click', e => {
    emit('click', e.latlng);
  });

  map.on('mousemove', e => {
    emit('mousemove', e.latlng);
  });

  map.on('mouseout', () => {
    emit('mouseleave');
  });

  map.on('moveend', () => {
    if (map) {
      emit('moveend', map.getBounds());
    }
  });
}

function setView(center: [number, number], zoom: number) {
  map?.setView(center, zoom);
}

function fitBounds(bounds: [[number, number], [number, number]]): boolean {
  if (!map) return false;
  map.fitBounds(bounds);
  return true;
}

function getCenter(): L.LatLng | undefined {
  return map?.getCenter();
}

function getZoom(): number | undefined {
  return map?.getZoom();
}

function getBounds(): L.LatLngBounds | undefined {
  return map?.getBounds();
}

function isReady(): boolean {
  return map !== null;
}

defineExpose({
  setView,
  fitBounds,
  getCenter,
  getZoom,
  getBounds,
  isReady
});
</script>

<template>
  <div ref="mapContainer" class="leaflet-map-container"></div>
</template>

<style>
.leaflet-map-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: var(--n-body-color);
}

/* Dark theme for Leaflet controls overrides can be handled centrally */
.leaflet-top,
.leaflet-bottom {
  z-index: 1000;
}

.leaflet-left .leaflet-control-zoom {
  margin-top: 18px;
  margin-left: 18px;
}

.leaflet-control-zoom {
  overflow: hidden;
  border: 1px solid rgb(148 163 184 / 24%) !important;
  border-radius: 18px !important;
  background: rgb(255 255 255 / 72%);
  box-shadow: 0 16px 36px rgb(15 23 42 / 16%);
  backdrop-filter: blur(14px);
}

.leaflet-control-zoom a {
  width: 42px;
  height: 42px;
  line-height: 42px;
  background: transparent !important;
  color: var(--n-text-color) !important;
  border: none !important;
  font-size: 18px;
  font-weight: 600;
}

.leaflet-control-zoom a + a {
  border-top: 1px solid rgb(148 163 184 / 20%) !important;
}

.leaflet-control-zoom a:hover {
  background: rgb(255 255 255 / 24%) !important;
}

.leaflet-control-scale-line {
  border: 1px solid rgb(148 163 184 / 24%) !important;
  border-radius: 999px;
  background: rgb(255 255 255 / 66%) !important;
  color: var(--n-text-color) !important;
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 24px rgb(15 23 42 / 10%);
}

.leaflet-control-attribution {
  border-radius: 999px 0 0 0;
  background: rgb(255 255 255 / 58%) !important;
  color: var(--n-text-color-3) !important;
  backdrop-filter: blur(10px);
}

.leaflet-control-attribution a {
  color: var(--n-primary-color) !important;
}

.leaflet-tile-pane {
  opacity: 1;
  transition: opacity 0.2s;
}

.leaflet-container {
  font-family: inherit;
  z-index: 10;
}

.dark .leaflet-control-zoom {
  border-color: rgb(148 163 184 / 18%) !important;
  background: rgb(17 24 39 / 72%);
}

.dark .leaflet-control-zoom a:hover {
  background: rgb(255 255 255 / 8%) !important;
}

.dark .leaflet-control-scale-line {
  border-color: rgb(148 163 184 / 18%) !important;
  background: rgb(17 24 39 / 68%) !important;
}

.dark .leaflet-control-attribution {
  background: rgb(17 24 39 / 56%) !important;
}
</style>
