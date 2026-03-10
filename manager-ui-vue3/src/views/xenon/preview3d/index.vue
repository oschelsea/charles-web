<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import {
  NCard,
  NSpace,
  NSelect,
  NButton,
  NInputNumber,
  NCollapse,
  NCollapseItem,
  NEmpty,
  NSpin,
  useMessage
} from 'naive-ui';
import type { SelectOption } from 'naive-ui';
import { layerApi, type Layer } from '@/service/api/xenon/layer';

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
        label: `🏗️ ${l.workspaceName ? l.workspaceName + ':' : ''}${l.title || l.name}`,
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
    const fullLayers: Layer[] = [];
    for (const summary of response?.data?.layers || []) {
      try {
        const detail = await layerApi.getByName(summary.name);
        if (detail?.data?.layer) {
          fullLayers.push(detail.data.layer);
        }
      } catch {
        // Skip
      }
    }
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
    // @ts-ignore
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
      destination: Cesium.Cartesian3.fromDegrees(
        cameraLongitude.value,
        cameraLatitude.value,
        cameraHeight.value
      )
    });
    
    viewer.imageryLayers.addImageryProvider(
      new Cesium.UrlTemplateImageryProvider({
        url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
      })
    );
    
    if (selectedTileset.value) {
      handleLoadTileset();
    }
    
  } catch (e) {
    console.warn('Cesium not available or initialization failed', e);
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
  
  // @ts-ignore
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
  
  // @ts-ignore
  import('cesium').then(Cesium => {
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(
        cameraLongitude.value,
        cameraLatitude.value,
        cameraHeight.value
      ),
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
  <div class="p-4 h-full">
    <div class="flex h-full gap-4 preview-3d">
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
                <NButton 
                  type="primary" 
                  :disabled="!selectedTileset"
                  @click="handleLoadTileset"
                  block
                >
                  加载Tileset
                </NButton>
              </NSpace>
            </NCollapseItem>

            <NCollapseItem title="相机位置" name="camera">
              <NSpace vertical size="small">
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">经度:</span>
                  <NInputNumber
                    v-model:value="cameraLongitude"
                    :min="-180"
                    :max="180"
                    :step="0.1"
                    size="small"
                  />
                </div>
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">纬度:</span>
                  <NInputNumber
                    v-model:value="cameraLatitude"
                    :min="-90"
                    :max="90"
                    :step="0.1"
                    size="small"
                  />
                </div>
                <div class="flex items-center gap-2">
                  <span class="w-12 text-[13px] text-[var(--n-text-color-3)]">高度:</span>
                  <NInputNumber
                    v-model:value="cameraHeight"
                    :min="100"
                    :max="10000000"
                    :step="1000"
                    size="small"
                  />
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
              <code class="block mt-[2px] text-primary font-mono text-[11px] bg-[var(--n-border-color)] px-1 py-0.5 rounded">/3dtiles/{name}/tileset.json</code>
            </div>
            <div class="mb-2">
              <span class="text-[var(--n-text-color-3)]">B3DM:</span>
              <code class="block mt-[2px] text-primary font-mono text-[11px] bg-[var(--n-border-color)] px-1 py-0.5 rounded">/3dtiles/{name}/*.b3dm</code>
            </div>
            <div class="mb-2">
              <span class="text-[var(--n-text-color-3)]">PNTS:</span>
              <code class="block mt-[2px] text-primary font-mono text-[11px] bg-[var(--n-border-color)] px-1 py-0.5 rounded">/3dtiles/{name}/*.pnts</code>
            </div>
          </div>
        </NCard>
      </div>

      <div class="flex-1 rounded-xl overflow-hidden shadow-sm border border-gray-200 dark:border-gray-800">
        <NSpin :show="loading" class="h-full">
          <div ref="cesiumContainer" class="w-full h-full min-h-[400px] bg-[#1a1a2e] flex items-center justify-center">
            <NEmpty v-if="!loading && !viewer" description="Cesium 未加载">
              <template #extra>
                <p class="text-[rgba(255,255,255,0.5)] text-[12px]">
                  未找到Cesium容器。
                </p>
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
