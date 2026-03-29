<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  NButton,
  NCard,
  NCollapse,
  NCollapseItem,
  NEmpty,
  NInputNumber,
  NSelect,
  NSpace,
  NSpin,
  useMessage
} from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import type { Layer, LayerSummary } from '@/service/api/xenon/layer';
import { layerApi } from '@/service/api/xenon/layer';

const route = useRoute();
const message = useMessage();

const loading = ref(true);
const tilesetLoading = ref(false);
const cesiumContainer = ref<HTMLDivElement | null>(null);
const controlsCollapsed = ref(Boolean(route.query.layer));
const infoCollapsed = ref(true);

const availableLayers = ref<Layer[]>([]);
const selectedTileset = ref<string | null>(null);
const hasActiveTileset = ref(false);

const cameraLongitude = ref(117);
const cameraLatitude = ref(35);
const cameraHeight = ref(10000);

const statusLongitude = ref<number | null>(null);
const statusLatitude = ref<number | null>(null);
const statusHeight = ref<number | null>(null);

let viewer: any = null;
let activeTileset: any = null;
let removeCameraChangedListener: (() => void) | null = null;

const tilesetOptions = computed<SelectOption[]>(() =>
  availableLayers.value
    .filter(layer => layer.type === 'TILES3D')
    .map(layer => {
      const qualifiedName = layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
      return {
        label: `🏗️ ${layer.workspaceName ? `${layer.workspaceName}:` : ''}${layer.title || layer.name}`,
        value: qualifiedName
      };
    })
);

const statusLongitudeText = computed(() => formatCoordinate(statusLongitude.value));
const statusLatitudeText = computed(() => formatCoordinate(statusLatitude.value));
const statusHeightText = computed(() => formatHeight(statusHeight.value));
const selectedLayerInfo = computed(() => {
  if (!selectedTileset.value) return null;

  return availableLayers.value.find(layer => getQualifiedLayerName(layer) === selectedTileset.value) ?? null;
});
const currentTilesetEndpoint = computed(() =>
  selectedTileset.value ? buildTilesetUrl(selectedTileset.value) : '/services/{workspace}:{layer}/3dtiles/tileset.json'
);

function formatCoordinate(value: number | null) {
  return value === null || value === undefined ? '--' : value.toFixed(6);
}

function formatHeight(value: number | null) {
  if (value === null || value === undefined) return '--';
  return value >= 1000 ? `${(value / 1000).toFixed(2)} km` : `${value.toFixed(2)} m`;
}

function getQualifiedLayerName(layer: Pick<Layer, 'name' | 'workspaceName'>) {
  return layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
}

function buildTilesetUrl(layerName: string) {
  const contextPath = import.meta.env.VITE_CONTEXT_PATH || '/xenon';
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${baseUrl}${contextPath}/services/${layerName}/3dtiles/tileset.json`;
}

function resolveRouteLayer(layerParam?: string) {
  if (!layerParam) return null;

  const exactMatch = availableLayers.value.find(
    layer => layer.type === 'TILES3D' && getQualifiedLayerName(layer) === layerParam
  );
  if (exactMatch) {
    return getQualifiedLayerName(exactMatch);
  }

  const layerName = layerParam.includes(':') ? layerParam.split(':').pop() : layerParam;
  const fallbackMatch = availableLayers.value.find(layer => layer.type === 'TILES3D' && layer.name === layerName);

  return fallbackMatch ? getQualifiedLayerName(fallbackMatch) : null;
}

function clearActiveTileset() {
  if (!viewer || !activeTileset) return;

  viewer.scene.primitives.remove(activeTileset);
  if (typeof activeTileset.destroy === 'function' && !activeTileset.isDestroyed?.()) {
    activeTileset.destroy();
  }

  activeTileset = null;
  hasActiveTileset.value = false;
}

function updateViewStatus(Cesium: any) {
  if (!viewer) return;

  const scene = viewer.scene;
  const ellipsoid = scene.globe.ellipsoid;
  const cameraCartographic = ellipsoid.cartesianToCartographic(viewer.camera.position);

  if (cameraCartographic) {
    statusHeight.value = cameraCartographic.height;
  }

  const canvas = scene.canvas;
  const center = new Cesium.Cartesian2(canvas.clientWidth / 2, canvas.clientHeight / 2);
  const ray = viewer.camera.getPickRay(center);
  const pickedCartesian = ray ? scene.globe.pick(ray, scene) : null;
  const targetCartographic = pickedCartesian ? ellipsoid.cartesianToCartographic(pickedCartesian) : cameraCartographic;

  if (!targetCartographic) return;

  statusLongitude.value = Cesium.Math.toDegrees(targetCartographic.longitude);
  statusLatitude.value = Cesium.Math.toDegrees(targetCartographic.latitude);
}

function bindCameraStatus(Cesium: any) {
  if (!viewer) return;

  const handleCameraChanged = () => updateViewStatus(Cesium);

  removeCameraChangedListener?.();
  viewer.camera.percentageChanged = 0.01;
  viewer.camera.changed.addEventListener(handleCameraChanged);
  removeCameraChangedListener = () => {
    viewer?.camera?.changed.removeEventListener(handleCameraChanged);
  };

  updateViewStatus(Cesium);
}

function focusTileset(Cesium: any, tileset: any) {
  const sphere = tileset?.boundingSphere;

  if (!viewer || !sphere || !(sphere.radius > 0)) return;

  viewer.camera.flyToBoundingSphere(sphere, {
    offset: new Cesium.HeadingPitchRange(0, Cesium.Math.toRadians(-35), sphere.radius * 2.5),
    duration: 1.5
  });
}

async function loadLayers() {
  try {
    const response = (await layerApi.getAll()) as { data?: { layers?: LayerSummary[] }; layers?: LayerSummary[] };
    let summaries: LayerSummary[] = [];
    if (Array.isArray(response.data?.layers)) {
      summaries = response.data.layers;
    } else if (Array.isArray(response.layers)) {
      summaries = response.layers;
    }

    const detailResults: Array<Layer | null> = await Promise.all(
      summaries.map(async summary => {
        try {
          const detail = (await layerApi.getByName(summary.name)) as { data?: { layer?: Layer }; layer?: Layer };
          return detail.data?.layer ?? detail.layer ?? null;
        } catch {
          return null;
        }
      })
    );

    const fullLayers = detailResults.filter((layer): layer is Layer => Boolean(layer));
    availableLayers.value = fullLayers;

    const routeLayer = resolveRouteLayer(route.query.layer as string | undefined);
    if (routeLayer) {
      selectedTileset.value = routeLayer;
    }
  } catch {
    message.error('加载图层列表失败');
  }
}

async function initViewer() {
  const Cesium = await import('cesium');

  if (!cesiumContainer.value) return;

  viewer = new Cesium.Viewer(cesiumContainer.value, {
    terrainProvider: undefined,
    baseLayer: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: true,
    navigationHelpButton: false,
    animation: false,
    timeline: false,
    fullscreenButton: true,
    vrButton: false,
    infoBox: false,
    selectionIndicator: false,
    shadows: false,
    shouldAnimate: true
  });

  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(cameraLongitude.value, cameraLatitude.value, cameraHeight.value)
  });

  viewer.imageryLayers.addImageryProvider(
    new Cesium.UrlTemplateImageryProvider({
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
    })
  );

  bindCameraStatus(Cesium);
}

async function loadTileset(layerName = selectedTileset.value) {
  if (!layerName || !viewer) return;

  tilesetLoading.value = true;

  try {
    const Cesium = await import('cesium');
    const url = buildTilesetUrl(layerName);

    clearActiveTileset();

    const tileset = await Cesium.Cesium3DTileset.fromUrl(url);
    activeTileset = viewer.scene.primitives.add(tileset);
    activeTileset.xenonLayerName = layerName;
    hasActiveTileset.value = true;
    selectedTileset.value = layerName;

    focusTileset(Cesium, tileset);
    updateViewStatus(Cesium);
  } catch (error: any) {
    clearActiveTileset();
    message.error(`加载 Tileset 失败: ${error?.message || error}`);
  } finally {
    tilesetLoading.value = false;
  }
}

async function handleLoadTileset() {
  await loadTileset();
}

async function handleFocusCurrentTileset() {
  if (!viewer || !activeTileset) return;

  const Cesium = await import('cesium');

  focusTileset(Cesium, activeTileset);
  updateViewStatus(Cesium);
}

async function handleFlyTo() {
  if (!viewer) return;

  const Cesium = await import('cesium');

  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(cameraLongitude.value, cameraLatitude.value, cameraHeight.value),
    duration: 2
  });

  updateViewStatus(Cesium);
}

function handleResetView() {
  cameraLongitude.value = 117;
  cameraLatitude.value = 35;
  cameraHeight.value = 10000;
  handleFlyTo().catch(() => undefined);
}

async function syncRouteLayer() {
  if (!viewer || !availableLayers.value.length) return;

  const routeLayer = resolveRouteLayer(route.query.layer as string | undefined);
  if (!routeLayer || routeLayer === activeTileset?.xenonLayerName) return;

  if (activeTileset) {
    activeTileset.xenonLayerName = null;
  }
  await loadTileset(routeLayer);
}

onMounted(async () => {
  loading.value = true;

  await loadLayers();

  try {
    await initViewer();

    await syncRouteLayer();
  } catch {
    message.error('Cesium 初始化失败');
  } finally {
    loading.value = false;
  }
});

watch(
  () => route.query.layer,
  async () => {
    await syncRouteLayer();
  }
);

onUnmounted(() => {
  removeCameraChangedListener?.();
  removeCameraChangedListener = null;
  clearActiveTileset();

  if (viewer) {
    viewer.destroy();
    viewer = null;
  }
});
</script>

<template>
  <div class="preview-3d">
    <NSpin :show="loading || tilesetLoading" class="viewer-spin">
      <div ref="cesiumContainer" class="cesium-container">
        <NEmpty v-if="!loading && !viewer" description="Cesium 未加载">
          <template #extra>
            <p class="empty-tip">请安装 Cesium: pnpm add cesium</p>
          </template>
        </NEmpty>
      </div>
    </NSpin>

    <div class="map-tint"></div>

    <div class="map-overlay">
      <div class="floating-panel floating-panel--left">
        <NButton
          v-if="controlsCollapsed"
          class="panel-pill"
          type="primary"
          strong
          secondary
          round
          @click="controlsCollapsed = false"
        >
          打开控制台
        </NButton>

        <NCard v-else class="floating-card control-card" :bordered="false" size="small">
          <template #header>
            <div class="panel-heading">
              <strong>地图工作台</strong>
            </div>
          </template>
          <template #header-extra>
            <NButton text size="small" @click="controlsCollapsed = true">收起</NButton>
          </template>

          <NCollapse :default-expanded-names="['tileset']" arrow-placement="right">
            <NCollapseItem title="图层控制" name="tileset">
              <NSpace vertical>
                <NSelect
                  v-model:value="selectedTileset"
                  :options="tilesetOptions"
                  placeholder="选择 3D Tiles 图层"
                  clearable
                />
                <NSpace>
                  <NButton
                    type="primary"
                    :disabled="!selectedTileset"
                    :loading="tilesetLoading"
                    @click="handleLoadTileset"
                  >
                    加载图层
                  </NButton>
                  <NButton :disabled="!hasActiveTileset" @click="handleFocusCurrentTileset">重新聚焦</NButton>
                </NSpace>
              </NSpace>
            </NCollapseItem>

            <NCollapseItem title="相机控制" name="camera">
              <NSpace vertical size="small">
                <div class="param-row">
                  <span class="param-label">经度</span>
                  <NInputNumber v-model:value="cameraLongitude" :min="-180" :max="180" :step="0.1" size="small" />
                </div>
                <div class="param-row">
                  <span class="param-label">纬度</span>
                  <NInputNumber v-model:value="cameraLatitude" :min="-90" :max="90" :step="0.1" size="small" />
                </div>
                <div class="param-row">
                  <span class="param-label">高度</span>
                  <NInputNumber v-model:value="cameraHeight" :min="100" :max="10000000" :step="1000" size="small" />
                </div>
                <NSpace>
                  <NButton size="small" @click="handleFlyTo">飞行至</NButton>
                  <NButton size="small" @click="handleResetView">重置</NButton>
                </NSpace>
              </NSpace>
            </NCollapseItem>
          </NCollapse>
        </NCard>
      </div>

      <div class="floating-panel floating-panel--right">
        <NButton v-if="infoCollapsed" class="panel-pill panel-pill--muted" round @click="infoCollapsed = false">
          图层信息
        </NButton>

        <NCard v-else class="floating-card info-card" :bordered="false" size="small">
          <template #header>
            <div class="panel-heading">
              <strong>图层信息</strong>
            </div>
          </template>
          <template #header-extra>
            <NButton text size="small" @click="infoCollapsed = true">收起</NButton>
          </template>

          <div class="info-stack">
            <div class="info-row">
              <span class="info-label">当前图层</span>
              <strong class="info-value">{{ selectedLayerInfo?.title || selectedTileset || '未加载' }}</strong>
            </div>
            <div class="info-row">
              <span class="info-label">图层名称</span>
              <span class="info-value">{{ selectedTileset || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">工作空间</span>
              <span class="info-value">{{ selectedLayerInfo?.workspaceName || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">图层类型</span>
              <span class="info-value">{{ selectedLayerInfo?.type || '--' }}</span>
            </div>
            <div class="info-block">
              <span class="info-label">Tileset URL</span>
              <code>{{ currentTilesetEndpoint }}</code>
            </div>
            <div class="info-block">
              <span class="info-label">服务约定</span>
              <code>/services/{workspace}:{layer}/3dtiles/tileset.json</code>
              <code>/services/{workspace}:{layer}/3dtiles/*.b3dm</code>
              <code>/services/{workspace}:{layer}/3dtiles/*.pnts</code>
            </div>
          </div>
        </NCard>
      </div>
    </div>

    <div class="status-bar">
      <span class="status-item">经度 {{ statusLongitudeText }}</span>
      <span class="status-item">纬度 {{ statusLatitudeText }}</span>
      <span class="status-item">高度 {{ statusHeightText }}</span>
    </div>
  </div>
</template>

<style scoped>
.preview-3d {
  --overlay-accent-color: var(--n-primary-color);
  --overlay-text-color: #1f2937;
  --overlay-muted-color: #6b7280;
  --overlay-base-bg: rgb(var(--layout-bg-color));
  --overlay-border-color: rgb(148 163 184 / 45%);
  --overlay-panel-bg: rgb(255 255 255 / 92%);
  --overlay-pill-bg: rgb(255 255 255 / 58%);
  --overlay-status-bg: rgb(255 255 255 / 20%);
  --overlay-code-bg: rgb(255 255 255 / 70%);
  --overlay-code-color: #0f172a;
  position: relative;
  height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
  min-height: 620px;
  overflow: hidden;
  background: var(--layout-bg-color);
  isolation: isolate;
}

:global(.dark) .preview-3d {
  --overlay-text-color: #e5e7eb;
  --overlay-muted-color: #9ca3af;
  --overlay-border-color: rgb(148 163 184 / 28%);
  --overlay-panel-bg: rgb(17 24 39 / 90%);
  --overlay-pill-bg: rgb(17 24 39 / 86%);
  --overlay-status-bg: rgb(2 6 23 / 26%);
  --overlay-code-bg: rgb(2 6 23 / 52%);
  --overlay-code-color: #cbd5e1;
}

.viewer-spin,
.viewer-spin :deep(.n-spin-content) {
  position: absolute;
  inset: 0;
  height: 100%;
  width: 100%;
}

.cesium-container {
  display: flex;
  min-height: 100%;
  height: 100%;
  width: 100%;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top, rgb(47 104 191 / 22%), transparent 34%),
    linear-gradient(180deg, #081120 0%, #050c16 100%);
}

.cesium-container :deep(.cesium-viewer),
.cesium-container :deep(.cesium-viewer-cesiumWidgetContainer),
.cesium-container :deep(.cesium-widget),
.cesium-container :deep(canvas) {
  height: 100%;
  width: 100%;
}

.map-tint {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      circle at top left,
      color-mix(in srgb, var(--overlay-accent-color) 14%, transparent),
      transparent 24%
    ),
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--n-color) 6%, transparent) 0%,
      transparent 26%,
      color-mix(in srgb, var(--n-color) 18%, transparent) 100%
    );
  pointer-events: none;
}

.map-overlay {
  position: absolute;
  inset: 0;
  padding: 24px;
  pointer-events: none;
}

.floating-panel {
  position: absolute;
  pointer-events: auto;
}

.floating-panel--left {
  top: 24px;
  left: 24px;
  width: min(340px, calc(100% - 48px));
}

.floating-panel--right {
  top: 24px;
  right: 24px;
  width: min(320px, calc(100% - 48px));
}

:deep(.floating-card) {
  border: 1px solid var(--overlay-border-color);
  background-color: var(--overlay-panel-bg);
  color: var(--overlay-text-color);
  box-shadow: 0 18px 48px rgb(15 23 42 / 18%);
}

:deep(.floating-card.n-card) {
  background-color: var(--overlay-panel-bg) !important;
}

:deep(.floating-card .n-card-header) {
  padding-bottom: 10px;
  color: var(--overlay-text-color);
}

:deep(.floating-card .n-card__content) {
  padding-top: 4px;
  color: var(--overlay-text-color);
}

:deep(.floating-card .n-collapse-item__header-main),
:deep(.floating-card .n-collapse-item-arrow),
:deep(.floating-card .n-base-selection-label),
:deep(.floating-card .n-input-number .n-input__input-el) {
  color: var(--overlay-text-color);
}

.control-card {
  width: 100%;
}

.info-card {
  width: 100%;
}

.panel-heading {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.panel-heading strong {
  color: var(--overlay-text-color);
  font-size: 14px;
}

:deep(.panel-pill) {
  border: 1px solid var(--overlay-border-color);
  background-color: var(--overlay-pill-bg) !important;
  color: var(--overlay-text-color);
  box-shadow: 0 10px 24px rgb(0 0 0 / 24%);
}

:deep(.panel-pill .n-button__content) {
  color: var(--overlay-text-color) !important;
  font-weight: 600;
}

:deep(.panel-pill--muted) {
  background-color: var(--overlay-pill-bg) !important;
  border-color: var(--overlay-border-color);
}

.status-bar {
  position: absolute;
  bottom: 6px;
  left: 50%;
  z-index: 2;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  justify-content: center;
  width: auto;
  max-width: calc(100% - 40px);
  border: none;
  border-radius: 999px;
  background: var(--overlay-status-bg);
  padding: 10px 14px;
  color: var(--overlay-text-color);
  font-size: 11px;
  line-height: 1.4;
  pointer-events: none;
}

.status-item {
  white-space: nowrap;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param-label {
  width: 50px;
  font-size: 13px;
  color: var(--overlay-muted-color);
}

.info-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.info-label {
  color: var(--overlay-muted-color);
  font-size: 12px;
  white-space: nowrap;
}

.info-value {
  text-align: right;
  color: var(--overlay-text-color);
  word-break: break-word;
}

.info-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-block code {
  display: block;
  overflow-wrap: anywhere;
  border: 1px solid var(--overlay-border-color);
  border-radius: 10px;
  background: var(--overlay-code-bg);
  padding: 8px 10px;
  color: var(--overlay-code-color);
  font-family: 'Fira Code', monospace;
  font-size: 11px;
}

.empty-tip {
  color: var(--overlay-muted-color);
  font-size: 12px;
}

@media (width <= 768px) {
  .preview-3d {
    min-height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
    height: auto;
  }

  .map-overlay {
    padding: 16px;
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
    bottom: 96px;
  }

  .control-card,
  .info-card {
    width: 100%;
  }

  .status-bar {
    bottom: calc(var(--soy-footer-height, 0px) + 6px);
    width: calc(100% - 24px);
    justify-content: flex-start;
    border-radius: 18px;
    padding: 10px 12px;
  }
}
</style>

<style>
html.dark .preview-3d,
body.dark .preview-3d,
.dark .preview-3d {
  --overlay-text-color: #e5e7eb;
  --overlay-muted-color: #9ca3af;
  --overlay-border-color: rgb(148 163 184 / 28%);
  --overlay-panel-bg: rgb(17 24 39 / 90%);
  --overlay-pill-bg: rgb(17 24 39 / 56%);
  --overlay-status-bg: rgb(2 6 23 / 26%);
  --overlay-code-bg: rgb(2 6 23 / 52%);
  --overlay-code-color: #cbd5e1;
}
</style>
