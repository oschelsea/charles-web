<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  NButton,
  NButtonGroup,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NEmpty,
  NInputNumber,
  NPopover,
  NSelect,
  NSpace,
  NSpin,
  NTooltip,
  useMessage
} from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import type { Layer, LayerSummary } from '@/service/api/xenon/layer';
import { layerApi } from '@/service/api/xenon/layer';
import IconLayers from '~icons/ion/layers-outline';
import IconCamera from '~icons/ion/camera-outline';
import IconInformation from '~icons/ion/information-circle-outline';

const route = useRoute();
const message = useMessage();

const loading = ref(true);
const tilesetLoading = ref(false);
const cesiumContainer = ref<HTMLDivElement | null>(null);

const showLayerPopover = ref(false);
const showCameraPopover = ref(false);
const showInfoPopover = ref(false);

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
            <p class="text-xs text-gray-400">请安装 Cesium: pnpm add cesium</p>
          </template>
        </NEmpty>
      </div>
    </NSpin>

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
                    v-model:value="selectedTileset"
                    :options="tilesetOptions"
                    placeholder="选择 3D Tiles 图层"
                    clearable
                  />
                  <NButtonGroup>
                    <NButton
                      type="primary"
                      :disabled="!selectedTileset"
                      :loading="tilesetLoading"
                      @click="handleLoadTileset"
                    >
                      加载
                    </NButton>
                    <NButton :disabled="!hasActiveTileset" @click="handleFocusCurrentTileset">聚焦</NButton>
                  </NButtonGroup>
                </NSpace>
              </NCard>
            </NPopover>
          </template>
          图层控制
        </NTooltip>

        <NTooltip placement="right" :disabled="showCameraPopover">
          <template #trigger>
            <NPopover v-model:show="showCameraPopover" trigger="click" placement="right-start" :width="280">
              <template #trigger>
                <NButton :type="showCameraPopover ? 'primary' : 'default'" quaternary>
                  <template #icon>
                    <IconCamera />
                  </template>
                </NButton>
              </template>
              <NCard size="small" :bordered="false">
                <template #header>相机控制</template>
                <NSpace vertical>
                  <NSpace align="center">
                    <span class="w-8 text-xs text-gray-500">经度</span>
                    <NInputNumber v-model:value="cameraLongitude" :min="-180" :max="180" :step="0.1" size="small" />
                  </NSpace>
                  <NSpace align="center">
                    <span class="w-8 text-xs text-gray-500">纬度</span>
                    <NInputNumber v-model:value="cameraLatitude" :min="-90" :max="90" :step="0.1" size="small" />
                  </NSpace>
                  <NSpace align="center">
                    <span class="w-8 text-xs text-gray-500">高度</span>
                    <NInputNumber v-model:value="cameraHeight" :min="100" :max="10000000" :step="1000" size="small" />
                  </NSpace>
                  <NButtonGroup>
                    <NButton size="small" @click="handleFlyTo">飞行至</NButton>
                    <NButton size="small" @click="handleResetView">重置</NButton>
                  </NButtonGroup>
                </NSpace>
              </NCard>
            </NPopover>
          </template>
          相机控制
        </NTooltip>

        <NTooltip placement="right" :disabled="showInfoPopover">
          <template #trigger>
            <NPopover v-model:show="showInfoPopover" trigger="click" placement="right-start" :width="340">
              <template #trigger>
                <NButton :type="showInfoPopover ? 'primary' : 'default'" quaternary>
                  <template #icon>
                    <IconInformation />
                  </template>
                </NButton>
              </template>
              <NCard size="small" :bordered="false">
                <template #header>图层信息</template>
                <NDescriptions label-placement="left" :column="1" size="small">
                  <NDescriptionsItem label="当前图层">
                    {{ selectedLayerInfo?.title || selectedTileset || '未加载' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="图层名称">
                    {{ selectedTileset || '--' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="工作空间">
                    {{ selectedLayerInfo?.workspaceName || '--' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="图层类型">
                    {{ selectedLayerInfo?.type || '--' }}
                  </NDescriptionsItem>
                </NDescriptions>
                <div class="mt-2">
                  <div class="mb-1 text-xs text-gray-500">Tileset URL</div>
                  <code class="break-all text-xs">{{ currentTilesetEndpoint }}</code>
                </div>
              </NCard>
            </NPopover>
          </template>
          图层信息
        </NTooltip>
      </NButtonGroup>
    </div>

    <div class="status-bar">
      <span>经度 {{ statusLongitudeText }}</span>
      <span>纬度 {{ statusLatitudeText }}</span>
      <span>高度 {{ statusHeightText }}</span>
    </div>
  </div>
</template>

<style scoped>
.preview-3d {
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

.cesium-container {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #081120 0%, #050c16 100%);
}

.cesium-container :deep(.cesium-viewer),
.cesium-container :deep(canvas) {
  height: 100%;
  width: 100%;
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
  bottom: 6px;
  left: 50%;
  z-index: 2;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  border-radius: 999px;
  background: rgb(0 0 0 / 40%);
  padding: 8px 14px;
  color: #fff;
  font-size: 11px;
  pointer-events: none;
}

@media (width <= 768px) {
  .preview-3d {
    min-height: calc(100vh - var(--soy-header-height) - var(--soy-tab-height) - var(--calc-footer-height, 0px));
  }

  .toolbar {
    top: 16px;
    left: 16px;
  }

  .status-bar {
    padding: 6px 12px;
    font-size: 10px;
  }
}
</style>
