<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
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
import { type Layer, layerApi } from '@/service/api/xenon/layer';

const route = useRoute();
const message = useMessage();

const loading = ref(true);
const cesiumContainer = ref<HTMLDivElement>();
let viewer: any = null;

const availableLayers = ref<Layer[]>([]);
const selectedTileset = ref<string | null>(null);

const tilesetOptions = computed<SelectOption[]>(() =>
  availableLayers.value
    .filter(l => l.type === 'TILES3D')
    .map(l => {
      const qualifiedName = l.workspaceName ? `${l.workspaceName}:${l.name}` : l.name;
      return {
        label: `🏗️ ${l.workspaceName ? `${l.workspaceName}:` : ''}${l.title || l.name}`,
        value: qualifiedName
      };
    })
);

const cameraLongitude = ref(117);
const cameraLatitude = ref(35);
const cameraHeight = ref(10000);

onMounted(async () => {
  loading.value = true;

  try {
    const response = await layerApi.getAll();
    const summaries = response?.data?.layers || [];
    const detailPromises = summaries.map(async summary => {
      try {
        const detail = await layerApi.getByName(summary.name);
        return detail?.data?.layer;
      } catch {
        return null;
      }
    });

    const results = await Promise.all(detailPromises);
    const fullLayers = results.filter((l): l is Layer => Boolean(l));
    availableLayers.value = fullLayers;

    const layerParam = route.query.layer as string;
    if (layerParam) {
      const layerName = layerParam.includes(':') ? layerParam.split(':')[1] : layerParam;
      const exists = fullLayers.find(l => l.name === layerName && l.type === 'TILES3D');
      if (exists) {
        selectedTileset.value = layerParam;
      }
    }
  } catch {
    message.error('加载图层列表失败');
  }

  try {
    // @ts-expect-error: Cesium is not explicitly typed in this dynamic import
    const Cesium = await import('cesium');

    if (!cesiumContainer.value) return;

    viewer = new Cesium.Viewer(cesiumContainer.value, {
      terrainProvider: undefined,
      baseLayerPicker: false,
      geocoder: false,
      homeButton: false,
      sceneModePicker: true,
      navigationHelpButton: false,
      animation: false,
      timeline: false,
      fullscreenButton: true,
      vrButton: false,
      infoBox: true,
      selectionIndicator: true,
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

    if (selectedTileset.value) {
      handleLoadTileset();
    }
  } catch {
    // Silent
  } finally {
    loading.value = false;
  }
});

onUnmounted(() => {
  if (viewer) {
    viewer.destroy();
    viewer = null;
  }
});

function handleLoadTileset() {
  if (!selectedTileset.value || !viewer) return;

  const basePath = `${window.location.protocol}//${window.location.host}${import.meta.env.VITE_SERVICE_BASE_URL}`;
  const CONTEXT_PATH = '/xenon';
  const url = `${basePath}${CONTEXT_PATH}/services/${selectedTileset.value}/3dtiles/tileset.json`;

  // @ts-expect-error: Cesium dynamic import lacks explicit type definitions
  import('cesium').then(async Cesium => {
    try {
      const tileset = await Cesium.Cesium3DTileset.fromUrl(url);
      viewer.scene.primitives.add(tileset);
      viewer.zoomTo(tileset);
    } catch {
      message.error('加载Tileset失败');
    }
  });
}

function handleFlyTo() {
  if (!viewer) return;

  // @ts-expect-error: Cesium dynamic import lacks explicit type definitions
  import('cesium').then(Cesium => {
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(cameraLongitude.value, cameraLatitude.value, cameraHeight.value),
      duration: 2
    });
  });
}

function handleResetView() {
  cameraLongitude.value = 117;
  cameraLatitude.value = 35;
  cameraHeight.value = 10000;
  handleFlyTo();
}
</script>

<template>
  <div class="h-full p-4">
    <div class="preview-3d h-full flex gap-4">
      <div class="w-80 flex-shrink-0 overflow-y-auto">
        <NCard title="3D控制" :bordered="false" size="small">
          <NCollapse default-expanded-names="tileset" arrow-placement="right">
            <NCollapseItem title="3D Tilesets" name="tileset">
              <NSpace vertical>
                <NSelect
                  v-model:value="selectedTileset"
                  :options="tilesetOptions"
                  placeholder="选择3D Tileset"
                  clearable
                />
                <NButton type="primary" :disabled="!selectedTileset" block @click="handleLoadTileset">
                  加载Tileset
                </NButton>
              </NSpace>
            </NCollapseItem>

            <NCollapseItem title="相机位置" name="camera">
              <NSpace vertical size="small">
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">经度:</span>
                  <NInputNumber v-model:value="cameraLongitude" :min="-180" :max="180" :step="0.1" size="small" />
                </div>
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">纬度:</span>
                  <NInputNumber v-model:value="cameraLatitude" :min="-90" :max="90" :step="0.1" size="small" />
                </div>
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">高度:</span>
                  <NInputNumber v-model:value="cameraHeight" :min="100" :max="10000000" :step="1000" size="small" />
                </div>
                <NSpace mt-2>
                  <NButton size="small" @click="handleFlyTo">飞行至</NButton>
                  <NButton size="small" @click="handleResetView">重置</NButton>
                </NSpace>
              </NSpace>
            </NCollapseItem>
          </NCollapse>
        </NCard>

        <NCard title="服务端点" :bordered="false" size="small" class="mt-4">
          <div class="text-[12px]">
            <div class="mb-2">
              <span class="text-[var(--n-text-color-3)]">Tileset:</span>
              <code
                class="mt-[2px] block rounded bg-[var(--n-border-color)] px-1 py-0.5 text-[11px] text-primary font-mono"
              >
                /3dtiles/{name}/tileset.json
              </code>
            </div>
            <div class="mb-2">
              <span class="text-[var(--n-text-color-3)]">B3DM:</span>
              <code
                class="mt-[2px] block rounded bg-[var(--n-border-color)] px-1 py-0.5 text-[11px] text-primary font-mono"
              >
                /3dtiles/{name}/*.b3dm
              </code>
            </div>
            <div class="mb-2">
              <span class="text-[var(--n-text-color-3)]">PNTS:</span>
              <code
                class="mt-[2px] block rounded bg-[var(--n-border-color)] px-1 py-0.5 text-[11px] text-primary font-mono"
              >
                /3dtiles/{name}/*.pnts
              </code>
            </div>
          </div>
        </NCard>
      </div>

      <div class="flex-1 overflow-hidden border border-gray-200 rounded-xl shadow-sm dark:border-gray-800">
        <NSpin :show="loading" class="h-full">
          <div ref="cesiumContainer" class="h-full min-h-[400px] w-full flex items-center justify-center bg-[#1a1a2e]">
            <NEmpty v-if="!loading && !viewer" description="Cesium 未加载">
              <template #extra>
                <p class="text-[12px] text-[rgba(255,255,255,0.5)]">未找到Cesium容器。</p>
              </template>
            </NEmpty>
          </div>
        </NSpin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.preview-3d {
  height: calc(100vh - 120px);
}
</style>
