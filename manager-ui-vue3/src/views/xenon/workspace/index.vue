<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NDrawer,
  NDrawerContent,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSpace,
  NSpin,
  NSwitch,
  NTabPane,
  NTabs,
  useDialog,
  useMessage
} from 'naive-ui';
import type { DataTableColumns, FormInst } from 'naive-ui';
import type { Workspace } from '@/service/api/xenon/workspace';
import { type DataStore, dataStoreApi, dataStoreTypes } from '@/service/api/xenon/datastore';
import { type Layer, layerApi, layerTypes } from '@/service/api/xenon/layer';
import { useWorkspaceStore } from '@/store/modules/xenon/workspace';

const message = useMessage();
const dialog = useDialog();
const workspaceStore = useWorkspaceStore();
const formRef = ref<FormInst | null>(null);

const searchText = ref('');
const showModal = ref(false);
const isEditing = ref(false);
const editingWorkspace = ref<Partial<Workspace>>({
  name: '',
  namespaceUri: '',
  description: '',
  enabled: true
});

// Computed filtered workspaces
const filteredWorkspaces = computed(() => {
  if (!searchText.value) return workspaceStore.workspaces;
  const search = searchText.value.toLowerCase();
  return workspaceStore.workspaces.filter(
    ws => ws.name.toLowerCase().includes(search) || ws.description?.toLowerCase().includes(search)
  );
});

// Detail Drawer State
const showDetail = ref(false);
const detailWorkspaceName = ref('');
const loadingDetail = ref(false);
const datastores = ref<DataStore[]>([]);
const layers = ref<Layer[]>([]);

const columns: DataTableColumns<Workspace> = [
  {
    title: '名称',
    key: 'name',
    sorter: 'default',
    render(row) {
      return h(
        'a',
        {
          style: { color: 'var(--n-primary-color)', cursor: 'pointer', fontWeight: 'bold' },
          onClick: () => openDetail(row.name)
        },
        { default: () => row.name }
      );
    }
  },
  {
    title: '命名空间URI',
    key: 'namespaceUri',
    ellipsis: { tooltip: true }
  },
  {
    title: '描述',
    key: 'description',
    ellipsis: { tooltip: true }
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
    width: 180,
    render(row) {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                onClick: () => handleEdit(row)
              },
              { default: () => '编辑' }
            ),
            h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                type: 'error',
                onClick: () => handleDelete(row)
              },
              { default: () => '删除' }
            )
          ]
        }
      );
    }
  }
];

onMounted(() => {
  loadWorkspaces();
});

async function loadWorkspaces() {
  try {
    await workspaceStore.fetchWorkspaces();
  } catch {
    message.error('加载工作空间失败');
  }
}

function handleCreate() {
  isEditing.value = false;
  editingWorkspace.value = {
    name: '',
    namespaceUri: '',
    description: '',
    enabled: true
  };
  showModal.value = true;
}

function handleEdit(workspace: Workspace) {
  isEditing.value = true;
  editingWorkspace.value = { ...workspace };
  showModal.value = true;
}

function handleDelete(workspace: Workspace) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除工作空间 "${workspace.name}" 吗？此操作不可撤销。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await workspaceStore.deleteWorkspace(workspace.name);
        message.success('删除成功');
      } catch {
        message.error('删除失败');
      }
    }
  });
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return false;
  }

  try {
    if (isEditing.value) {
      await workspaceStore.updateWorkspace(editingWorkspace.value.name!, editingWorkspace.value);
      message.success('更新成功');
    } else {
      await workspaceStore.createWorkspace(editingWorkspace.value);
      message.success('创建成功');
    }
    showModal.value = false;
    return true;
  } catch {
    message.error('操作失败');
    return false;
  }
}

// Detail Section Logic
async function openDetail(name: string) {
  detailWorkspaceName.value = name;
  showDetail.value = true;
  await loadDetailData();
}

async function loadDetailData() {
  loadingDetail.value = true;
  try {
    const dsRes = await dataStoreApi.getByWorkspace(detailWorkspaceName.value);
    const dsSummaries = dsRes?.data?.dataStores || [];
    const dsDetails = await Promise.all(
      dsSummaries.map(s =>
        dataStoreApi
          .getByName(detailWorkspaceName.value, s.name)
          .then(r => r.data?.dataStore)
          .catch(() => null)
      )
    );
    datastores.value = dsDetails.filter((d): d is DataStore => Boolean(d));

    const layerRes = await layerApi.getByWorkspace(detailWorkspaceName.value);
    const layerSummaries = layerRes?.data?.layers || [];
    const layerDetails = await Promise.all(
      layerSummaries.map(s =>
        layerApi
          .getByName(s.name)
          .then(r => r.data?.layer)
          .catch(() => null)
      )
    );
    layers.value = layerDetails.filter((l): l is Layer => Boolean(l));
  } catch {
    message.error('加载工作空间详情失败');
  } finally {
    loadingDetail.value = false;
  }
}

const dsColumns: DataTableColumns<DataStore> = [
  { title: '名称', key: 'name' },
  {
    title: '类型',
    key: 'type',
    render(row) {
      const meta = dataStoreTypes[row.type];
      return meta ? `${meta.icon} ${meta.label}` : row.type;
    }
  },
  {
    title: '状态',
    key: 'enabled',
    render(row) {
      return row.enabled ? '启用' : '禁用';
    }
  }
];

const layerColumns: DataTableColumns<Layer> = [
  { title: '名称', key: 'name' },
  {
    title: '类型',
    key: 'type',
    render(row) {
      const meta = layerTypes[row.type];
      return meta ? `${meta.icon} ${meta.label}` : row.type;
    }
  },
  { title: '标题', key: 'title' }
];
</script>

<template>
  <div class="p-4">
    <NSpace vertical :size="16">
      <NCard title="工作空间管理" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
        <template #header-extra>
          <NSpace>
            <NInput v-model:value="searchText" placeholder="搜索工作空间..." clearable class="w-200px" />
            <NButton type="primary" @click="handleCreate">
              <template #icon>
                <div class="i-mdi-plus"></div>
              </template>
              创建工作空间
            </NButton>
            <NButton @click="loadWorkspaces">
              <template #icon>
                <div class="i-mdi-refresh"></div>
              </template>
              刷新
            </NButton>
          </NSpace>
        </template>
        <NDataTable
          :columns="columns"
          :data="filteredWorkspaces"
          :loading="workspaceStore.loading"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          striped
          class="sm:h-full"
        />
      </NCard>
    </NSpace>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      :title="isEditing ? '编辑工作空间' : '创建工作空间'"
      :positive-text="isEditing ? '保存' : '创建'"
      negative-text="取消"
      @positive-click="handleSubmit"
    >
      <NForm
        ref="formRef"
        :model="editingWorkspace"
        label-placement="left"
        label-width="100px"
        require-mark-placement="right-hanging"
      >
        <NFormItem label="名称" path="name" required>
          <NInput v-model:value="editingWorkspace.name" placeholder="输入工作空间名称" :disabled="isEditing" />
        </NFormItem>
        <NFormItem label="命名空间URI" path="namespaceUri">
          <NInput v-model:value="editingWorkspace.namespaceUri" placeholder="http://example.com/namespace" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="editingWorkspace.description" type="textarea" placeholder="输入描述信息" />
        </NFormItem>
        <NFormItem label="启用" path="enabled">
          <NSwitch v-model:value="editingWorkspace.enabled" />
        </NFormItem>
      </NForm>
    </NModal>

    <!-- Details Drawer -->
    <NDrawer v-model:show="showDetail" width="600" placement="right">
      <NDrawerContent :title="`工作空间详情: ${detailWorkspaceName}`" closable>
        <NSpin :show="loadingDetail">
          <NTabs type="line" animated>
            <NTabPane name="datastores" tab="数据存储">
              <NDataTable :columns="dsColumns" :data="datastores" :bordered="false" size="small" />
            </NTabPane>
            <NTabPane name="layers" tab="图层列表">
              <NDataTable :columns="layerColumns" :data="layers" :bordered="false" size="small" />
            </NTabPane>
          </NTabs>
        </NSpin>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<style scoped>
.card-wrapper {
  height: calc(100vh - 120px);
}
</style>
