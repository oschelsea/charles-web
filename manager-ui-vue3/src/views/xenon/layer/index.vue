<script setup lang="ts">
import { ref, onMounted, computed, h, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  NCard,
  NDataTable,
  NButton,
  NSpace,
  NInput,
  NModal,
  NForm,
  NFormItem,
  NSelect,
  NSwitch,
  NTag,
  NEmpty,
  NSpin,
  useMessage,
  useDialog,
  NDrawer,
  NDrawerContent,
  NDescriptions,
  NDescriptionsItem
} from 'naive-ui';
import type { DataTableColumns, SelectOption } from 'naive-ui';
import { layerApi, layerTypes, getPublishableResources, type Layer, type PublishableResource } from '@/service/api/xenon/layer';
import { useWorkspaceStore } from '@/store/modules/xenon/workspace';
import { dataStoreApi, dataStoreTypes, type DataStore } from '@/service/api/xenon/datastore';
import LeafletMap, { type WmtsLayerConfig } from '@/components/xenon-map/LeafletMap.vue';
import { buildWmtsTileUrl } from '@/service/api/xenon/wmts';

const router = useRouter();
const message = useMessage();
const dialog = useDialog();
const workspaceStore = useWorkspaceStore();

const loading = ref(false);
const layers = ref<Layer[]>([]);
const searchText = ref('');

const showModal = ref(false);
const isCreating = ref(false);

const selectedWorkspace = ref<string>('');
const selectedDataStore = ref<string>('');
const selectedResource = ref<string>('');
const datastores = ref<DataStore[]>([]);
const publishableResources = ref<PublishableResource[]>([]);
const loadingDatastores = ref(false);
const loadingResources = ref(false);

const editingLayer = ref<Partial<Layer>>({
  name: '',
  title: '',
  description: '',
  type: 'VECTOR',
  enabled: true,
  advertised: true,
  queryable: true
});

// Detail Drawer State
const showDetail = ref(false);
const detailLayerName = ref('');
const loadingDetail = ref(false);
const currentDetailLayer = ref<Layer | null>(null);

const wmtsLayers = computed<WmtsLayerConfig[]>(() => {
  if (!currentDetailLayer.value) return [];
  // For the proxy, base URL is implicit, handled by interceptors. 
  // However, WMTS map layers usually need full URLs in the frontend if unproxied, 
  // or proxy path + backend endpoint. We'll use the window location with /dev-api proxy prefix.
  const baseUrl = `${window.location.protocol}//${window.location.host}${import.meta.env.VITE_SERVICE_BASE_URL}`;
  return [{
      name: currentDetailLayer.value.name,
      url: buildWmtsTileUrl(currentDetailLayer.value.name, {
        baseUrl,
        tileMatrixSet: 'EPSG:3857',
        format: 'png'
      }),
      attribution: 'Xenon'
  }];
});

const filteredLayers = computed(() => {
  if (!searchText.value) return layers.value;
  const search = searchText.value.toLowerCase();
  return layers.value.filter(layer => 
    layer.name.toLowerCase().includes(search) ||
    layer.title?.toLowerCase().includes(search)
  );
});

const typeOptions = computed<SelectOption[]>(() =>
  Object.entries(layerTypes).map(([value, meta]) => ({
    label: `${meta.icon} ${meta.label}`,
    value
  }))
);

const workspaceOptions = computed<SelectOption[]>(() =>
  workspaceStore.workspaces.map(ws => ({
    label: ws.name,
    value: ws.name
  }))
);

const datastoreOptions = computed<SelectOption[]>(() =>
  datastores.value.map(ds => {
    const meta = dataStoreTypes[ds.type];
    return {
      label: `${meta?.icon || '📦'} ${ds.name} (${meta?.label || ds.type})`,
      value: ds.name
    };
  })
);

const resourceOptions = computed<SelectOption[]>(() =>
  publishableResources.value.map(res => ({
    label: `${res.type === 'vector' ? '📐' : '🖼️'} ${res.title || res.name}`,
    value: res.name
  }))
);

watch(selectedWorkspace, async (newVal) => {
  selectedDataStore.value = '';
  selectedResource.value = '';
  datastores.value = [];
  publishableResources.value = [];
  
  if (newVal) {
    loadingDatastores.value = true;
    try {
      const response = await dataStoreApi.getByWorkspace(newVal);
      const detailPromises = (response?.data?.dataStores || []).map(async (summary: any) => {
        try {
          const detail = await dataStoreApi.getByName(newVal, summary.name);
          return detail?.data?.dataStore;
        } catch {
          return null;
        }
      });
      const results = await Promise.all(detailPromises);
      datastores.value = results.filter((ds: any): ds is DataStore => !!ds);
    } catch {
      message.error('加载数据存储失败');
    } finally {
      loadingDatastores.value = false;
    }
  }
});

watch(selectedDataStore, async (newVal) => {
  selectedResource.value = '';
  publishableResources.value = [];
  
  if (newVal && selectedWorkspace.value) {
    const ds = datastores.value.find(d => d.name === newVal);
    if (ds?.type === 'TILES3D_CACHE' || ds?.type === 'ARCGIS_CACHE') {
      const type = ds.type === 'TILES3D_CACHE' ? 'TILES3D' : 'ARCGIS_CACHE';
      const resourceType = ds.type === 'TILES3D_CACHE' ? 'tiles3d' : 'arcgiscache';
      
      editingLayer.value.name = ds.name;
      editingLayer.value.title = ds.name;
      editingLayer.value.type = type;
      
      publishableResources.value = [{
        name: ds.name,
        nativeName: ds.name,
        type: resourceType as any,
        title: ds.name
      }];
      selectedResource.value = ds.name;
      return;
    } else if (ds?.type === 'GEOPACKAGE') {
      editingLayer.value.type = 'GEOPACKAGE_TILES';
    }
    
    loadingResources.value = true;
    try {
      const response = await getPublishableResources(selectedWorkspace.value, newVal);
      publishableResources.value = response.resources || [];
    } finally {
      loadingResources.value = false;
    }
  }
});

watch(selectedResource, (newVal) => {
  if (newVal) {
    const resource = publishableResources.value.find(r => r.name === newVal);
    if (resource) {
      editingLayer.value.name = resource.name;
      editingLayer.value.title = resource.title || resource.name;
      
      const ds = datastores.value.find(d => d.name === selectedDataStore.value);
      if (ds?.type === 'GEOPACKAGE') {
        editingLayer.value.type = 'GEOPACKAGE_TILES';
      } else if (resource.type as string === 'tiles3d') {
        editingLayer.value.type = 'TILES3D';
      } else if (resource.type as string === 'arcgiscache') {
        editingLayer.value.type = 'ARCGIS_CACHE';
      } else {
        editingLayer.value.type = resource.type === 'raster' ? 'RASTER' : 'VECTOR';
      }
    }
  }
});

function getLayerTypeMeta(type: string) {
  return layerTypes[type as keyof typeof layerTypes] || { label: type, icon: '❓', color: 'gray' };
}

const columns: DataTableColumns<Layer> = [
  {
    title: '图层名称',
    key: 'name',
    sorter: 'default',
    render(row) {
      const meta = layerTypes[row.type];
      const qualifiedName = row.workspaceName ? `${row.workspaceName}:${row.name}` : row.name;
      return h(
        'div',
        { 
          style: { cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }, 
          onClick: () => openDetail(qualifiedName) 
        }, 
        [
          h('span', { style: { fontSize: '18px' } }, meta?.icon || '📑'),
          h('span', { style: { fontWeight: 600, color: 'var(--n-primary-color)' } }, qualifiedName)
        ]
      );
    }
  },
  {
    title: '标题',
    key: 'title',
    ellipsis: { tooltip: true }
  },
  {
    title: '类型',
    key: 'type',
    width: 120,
    render(row) {
      const meta = layerTypes[row.type];
      return h(NTag, {
        type: row.type === 'VECTOR' ? 'info' : row.type === 'RASTER' ? 'warning' : 'default',
        round: true,
        size: 'small'
      }, { default: () => meta?.label || row.type });
    }
  },
  {
    title: '工作空间',
    key: 'workspaceName',
    width: 120,
    render(row) {
      return h(NTag, { type: 'default', size: 'small' }, { default: () => row.workspaceName || '-' });
    }
  },
  {
    title: '可访问',
    key: 'advertised',
    width: 80,
    render(row) {
      return h(NTag, {
        type: row.advertised ? 'success' : 'default',
        size: 'small'
      }, { default: () => row.advertised ? '是' : '否' });
    }
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render(row) {
      return h('span', {
        style: {
          display: 'inline-block',
          width: '10px',
          height: '10px',
          borderRadius: '50%',
          backgroundColor: row.enabled ? '#18a058' : '#d03050'
        }
      });
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render(row) {
      const buttons: ReturnType<typeof h>[] = [];
      const qualifiedName = row.workspaceName ? `${row.workspaceName}:${row.name}` : row.name;

      // TODO: Handle 3DTiles or WMS Preview correctly. For now we just view the detail drawer.
      buttons.push(
        h(NButton, {
          size: 'small',
          quaternary: true,
          type: 'success',
          onClick: () => openDetail(qualifiedName)
        }, { default: () => '预览' })
      );
      
      buttons.push(
        h(NButton, {
          size: 'small',
          quaternary: true,
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          quaternary: true,
          type: 'error',
          onClick: () => handleDelete(row)
        }, { default: () => '删除' })
      );
      
      return h(NSpace, { size: 'small' }, { default: () => buttons });
    }
  }
];

onMounted(async () => {
  await workspaceStore.fetchWorkspaces();
  await loadLayers();
});

async function loadLayers() {
  loading.value = true;
  try {
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
        // null
      }
    }
    layers.value = fullLayers;
  } catch {
    message.error('加载图层列表失败');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  isCreating.value = true;
  selectedWorkspace.value = '';
  selectedDataStore.value = '';
  selectedResource.value = '';
  datastores.value = [];
  publishableResources.value = [];
  editingLayer.value = {
    name: '',
    title: '',
    description: '',
    type: 'VECTOR',
    enabled: true,
    advertised: true,
    queryable: true
  };
  showModal.value = true;
}

function handleEdit(layer: Layer) {
  isCreating.value = false;
  editingLayer.value = { ...layer };
  showModal.value = true;
}

function handleDelete(layer: Layer) {
  const qualifiedName = layer.workspaceName ? `${layer.workspaceName}:${layer.name}` : layer.name;
  dialog.warning({
    title: '确认删除',
    content: `确定要删除图层 "${qualifiedName}" 吗？此操作不会删除原始数据。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await layerApi.delete(qualifiedName);
        await loadLayers();
        message.success('删除成功');
      } catch {
        message.error('删除失败');
      }
    }
  });
}

function validateName(name?: string, typeName = '图层') {
  if (!name || !name.trim()) return `${typeName}名称不能为空`;
  if (!/^[a-zA-Z0-9_-]+$/.test(name)) return `${typeName}名称只能包含字母、数字、下划线和连字符`;
  return null;
}

async function handleSubmit() {
  try {
    if (editingLayer.value.id) {
      const qualifiedName = editingLayer.value.workspaceName 
        ? `${editingLayer.value.workspaceName}:${editingLayer.value.name!}` 
        : editingLayer.value.name!;
      await layerApi.update(qualifiedName, editingLayer.value);
      message.success('更新成功');
    } else {
      const ws = workspaceStore.workspaces.find(w => w.name === selectedWorkspace.value);
      if (ws?.id) {
        editingLayer.value.workspaceId = ws.id;
      }
      const ds = datastores.value.find(d => d.name === selectedDataStore.value);
      if (ds?.id) {
        editingLayer.value.datastoreId = ds.id;
      }
      
      const error = validateName(editingLayer.value.name, '图层');
      if (error) {
        message.error(error);
        return;
      }
      
      await layerApi.create(editingLayer.value);
      message.success('发布成功');
    }
    showModal.value = false;
    await loadLayers();
  } catch {
    message.error('操作失败');
  }
}

async function openDetail(qualifiedName: string) {
  detailLayerName.value = qualifiedName;
  showDetail.value = true;
  await loadDetailData();
}

async function loadDetailData() {
  loadingDetail.value = true;
  try {
    const response = await layerApi.getByName(detailLayerName.value);
    currentDetailLayer.value = (response?.data as any)?.layer || null;
  } catch {
    message.error('获取图层详情失败');
  } finally {
    loadingDetail.value = false;
  }
}
</script>

<template>
  <div class="p-4">
    <NSpace vertical :size="16">
      <NCard title="图层管理" :bordered="false" size="small" class="sm:flex-1-hidden card-wrapper">
        <template #header-extra>
          <NSpace align="center">
            <NInput
              v-model:value="searchText"
              placeholder="搜索图层..."
              clearable
              style="width: 200px"
            />
            <NButton type="primary" @click="handleCreate">
              <template #icon>
                <div class="i-mdi-plus"></div>
              </template>
              发布新图层
            </NButton>
          </NSpace>
        </template>

        <NDataTable
          v-if="filteredLayers.length > 0"
          :columns="columns"
          :data="filteredLayers"
          :loading="loading"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          striped
          class="sm:h-full"
        />
        <NEmpty v-else description="暂无已发布的图层" />
      </NCard>
    </NSpace>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      :title="editingLayer.id ? '编辑图层' : '发布图层'"
      :positive-text="editingLayer.id ? '保存' : '发布'"
      negative-text="取消"
      style="width: 600px"
      @positive-click="handleSubmit"
    >
      <NForm
        :model="editingLayer"
        label-placement="left"
        label-width="100"
        require-mark-placement="right-hanging"
      >
        <template v-if="isCreating">
          <div class="section-divider">选择数据源</div>
          <NFormItem label="工作空间" required>
            <NSelect
              v-model:value="selectedWorkspace"
              :options="workspaceOptions"
              placeholder="选择工作空间"
              filterable
            />
          </NFormItem>
          <NFormItem label="数据存储" required>
            <NSpin :show="loadingDatastores" size="small">
              <NSelect
                v-model:value="selectedDataStore"
                :options="datastoreOptions"
                :disabled="!selectedWorkspace"
                placeholder="选择数据存储"
                filterable
              />
            </NSpin>
          </NFormItem>
          <NFormItem label="发布资源" required>
            <NSpin :show="loadingResources" size="small">
              <NSelect
                v-model:value="selectedResource"
                :options="resourceOptions"
                :disabled="!selectedDataStore"
                placeholder="选择要发布的资源"
                filterable
              />
            </NSpin>
          </NFormItem>
          <div class="section-divider">图层属性</div>
        </template>
        
        <template v-if="!isCreating && editingLayer.datastoreName">
          <NFormItem label="数据存储">
            <NTag size="small" type="info" :bordered="false">
              {{ editingLayer.datastoreName }}
              <template v-if="editingLayer.workspaceName">
                ({{ editingLayer.workspaceName }})
              </template>
            </NTag>
          </NFormItem>
        </template>
        
        <NFormItem label="图层名称" path="name" required>
          <NInput
            v-model:value="editingLayer.name"
            placeholder="输入图层名称"
            :disabled="!!editingLayer.id"
          />
        </NFormItem>
        <NFormItem label="标题" path="title">
          <NInput
            v-model:value="editingLayer.title"
            placeholder="输入显示标题"
          />
        </NFormItem>
        <NFormItem label="类型" path="type" required>
          <NSelect
            v-model:value="editingLayer.type"
            :options="typeOptions"
            placeholder="选择图层类型"
            :disabled="isCreating && !!selectedResource"
          />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput
            v-model:value="editingLayer.description"
            type="textarea"
            placeholder="输入描述信息"
          />
        </NFormItem>
        <NFormItem label="启用" path="enabled">
          <NSwitch v-model:value="editingLayer.enabled" />
        </NFormItem>
        <NFormItem label="公开访问" path="advertised">
          <NSwitch v-model:value="editingLayer.advertised" />
        </NFormItem>
        <NFormItem label="可查询" path="queryable">
          <NSwitch v-model:value="editingLayer.queryable" />
        </NFormItem>
      </NForm>
    </NModal>

    <!-- Details Drawer -->
    <NDrawer v-model:show="showDetail" width="700" placement="right">
      <NDrawerContent :title="currentDetailLayer?.title || currentDetailLayer?.name || '图层详情'" closable>
        <NSpin :show="loadingDetail">
          <template v-if="currentDetailLayer">
            <NSpace size="small" style="margin-bottom: 16px">
              <NTag :color="{ borderColor: getLayerTypeMeta(currentDetailLayer.type).color, textColor: getLayerTypeMeta(currentDetailLayer.type).color }" bordered>
                {{ getLayerTypeMeta(currentDetailLayer.type).icon }} {{ getLayerTypeMeta(currentDetailLayer.type).label }}
              </NTag>
              <NTag :type="currentDetailLayer.enabled ? 'success' : 'error'">
                {{ currentDetailLayer.enabled ? '已启用' : '已禁用' }}
              </NTag>
            </NSpace>
            
            <NDescriptions label-placement="left" :column="1" bordered size="small" style="margin-bottom: 24px">
              <NDescriptionsItem label="名称">{{ currentDetailLayer.name }}</NDescriptionsItem>
              <NDescriptionsItem label="类型">{{ currentDetailLayer.type }}</NDescriptionsItem>
              <NDescriptionsItem label="坐标系">{{ currentDetailLayer.srs || 'EPSG:4326' }}</NDescriptionsItem>
              <NDescriptionsItem label="数据存储">
                <span v-if="currentDetailLayer.datastoreName">
                  {{ currentDetailLayer.datastoreName }} 
                  <NTag size="small" v-if="currentDetailLayer.workspaceName" style="margin-left: 8px;">
                    {{ currentDetailLayer.workspaceName }}
                  </NTag>
                </span>
                <span v-else class="text-gray">-</span>
              </NDescriptionsItem>
            </NDescriptions>

            <div class="h-[400px] w-full border border-gray-200 dark:border-gray-800 rounded-md overflow-hidden">
              <LeafletMap
                :center="[35, 110]"
                :zoom="4"
                :wmts-layers="wmtsLayers"
                basemap="osm"
              />
            </div>
          </template>
        </NSpin>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<style scoped>
.card-wrapper {
  height: calc(100vh - 120px);
}

.section-divider {
  font-size: 14px;
  font-weight: 600;
  color: var(--n-text-color-3);
  margin: 16px 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--n-border-color);
}
</style>
