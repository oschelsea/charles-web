<script setup lang="ts">
import { computed, h, nextTick, onMounted, ref } from 'vue';
import {
  NButton,
  NCard,
  NCheckbox,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NInputGroup,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useDialog,
  useMessage
} from 'naive-ui';
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui';
import {
  type DataStore,
  type DataStoreType,
  type FieldConfig,
  dataStoreApi,
  dataStoreFieldConfigs,
  dataStoreTypes
} from '@/service/api/xenon/datastore';
import { fetchDirContent, fetchRootDirs } from '@/service/api/system/file';
import { useWorkspaceStore } from '@/store/modules/xenon/workspace';

const message = useMessage();
const dialog = useDialog();
const workspaceStore = useWorkspaceStore();

const loading = ref(false);
const dataStores = ref<DataStore[]>([]);
const selectedWorkspace = ref<string>('');

const showModal = ref(false);
const isEditing = ref(false);
const editingStore = ref<Partial<DataStore>>({
  name: '',
  description: '',
  workspaceName: '',
  type: 'SHAPEFILE',
  enabled: true,
  connectionParams: {}
});

const formRef = ref<FormInst | null>(null);

const rules: FormRules = {
  name: [
    { required: true, message: '数据存储名称不能为空', trigger: ['input', 'blur'] },
    {
      pattern: /^[a-zA-Z0-9_-]+$/,
      message: '数据存储名称只能包含字母、数字、下划线和连字符',
      trigger: ['input', 'blur']
    }
  ],
  workspaceName: [{ required: true, message: '请选择工作空间', trigger: ['change', 'blur'] }],
  type: [{ required: true, message: '请选择数据存储类型', trigger: ['change', 'blur'] }],
  connectionParams: {
    trigger: ['input', 'blur'],
    validator(_rule, value) {
      const type = editingStore.value.type as DataStoreType;
      const fields = dataStoreFieldConfigs[type] || [];
      const params = value as Record<string, any>;

      for (const field of fields) {
        if (field.required && (params[field.key] === undefined || params[field.key] === '')) {
          return new Error(`${field.label}不能为空`);
        }
      }
      return true;
    }
  }
};

// 文件选择器状态
const showFileSelector = ref(false);
const currentFileField = ref<FieldConfig | null>(null);
const fileSelectorFilters = computed(() =>
  currentFileField.value?.fileFilter
    ? [currentFileField.value.fileFilter]
    : [{ label: '所有文件 (*.*)', value: '*.*', matchType: 'all' }]
);
const fileSelectorType = computed(() => currentFileField.value?.selectType || 'file');

function openFileSelector(field: FieldConfig) {
  currentFileField.value = field;
  showFileSelector.value = true;
}

async function onFileSelected(file: { path: string }) {
  if (currentFileField.value) {
    const key = currentFileField.value.key;
    (editingStore.value.connectionParams as Record<string, unknown>)[key] = file.path;

    // Auto-fetch terrain meta info when layer.json is selected for TERRAIN_CACHE
    if (key === 'layerJsonPath' && editingStore.value.type === 'TERRAIN_CACHE') {
      try {
        const metaInfo = await dataStoreApi.getTerrainMeta(file.path);
        if (metaInfo) {
          const { zipped } = metaInfo as any;
          // Auto-fill zipped field
          (editingStore.value.connectionParams as Record<string, unknown>).zipped = zipped ?? false;
        }
      } catch {
        // Ignore errors - meta.json might not exist
      }
    }
  }
  showFileSelector.value = false;
}

// Data store type options
const typeOptions = computed<SelectOption[]>(() =>
  Object.entries(dataStoreTypes).map(([value, meta]) => ({
    label: `${meta.icon} ${meta.label}`,
    value
  }))
);

// 工作空间选项
const workspaceOptions = computed<SelectOption[]>(() =>
  workspaceStore.workspaces.map(ws => ({
    label: ws.name,
    value: ws.name
  }))
);

// 根据工作空间过滤数据存储
const filteredDataStores = computed(() => {
  if (!selectedWorkspace.value) {
    return dataStores.value; // 未选择工作空间时显示全部
  }
  return dataStores.value.filter(ds => ds.workspaceName === selectedWorkspace.value);
});

// Current field configs based on selected type
const currentFields = computed<FieldConfig[]>(() => {
  const type = editingStore.value.type as DataStoreType;
  return type ? dataStoreFieldConfigs[type] || [] : [];
});

// Handle type change - reset connectionParams with default values
function handleTypeChange(type: DataStoreType) {
  const fields = dataStoreFieldConfigs[type] || [];
  const defaultParams: Record<string, unknown> = {};

  fields.forEach(field => {
    if (field.defaultValue !== undefined) {
      defaultParams[field.key] = field.defaultValue;
    }
  });

  editingStore.value.connectionParams = defaultParams;
}

const columns: DataTableColumns<DataStore> = [
  {
    title: '名称',
    key: 'name',
    render(row) {
      return h('span', { style: { fontWeight: '600' } }, row.name);
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
    title: '类型',
    key: 'type',
    width: 150,
    render(row) {
      const meta = dataStoreTypes[row.type];
      return h(
        NTag,
        {
          type: meta?.isVector ? 'info' : 'warning',
          round: true
        },
        { default: () => `${meta?.icon || ''} ${meta?.label || row.type}` }
      );
    }
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
    width: 150,
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

onMounted(async () => {
  await workspaceStore.fetchWorkspaces();
  await loadDataStores();
});

async function loadDataStores() {
  loading.value = true;
  try {
    const res = await dataStoreApi.getAll();
    dataStores.value = res?.data || [];
  } catch {
    message.error('加载数据存储失败');
    dataStores.value = [];
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  isEditing.value = false;
  editingStore.value = {
    name: '',
    description: '',
    type: 'SHAPEFILE',
    enabled: true,
    connectionParams: {}
  };
  showModal.value = true;
  nextTick(() => {
    formRef.value?.restoreValidation();
  });
}

function handleEdit(store: DataStore) {
  isEditing.value = true;
  editingStore.value = {
    ...store,
    connectionParams: store.connectionParams || {}
  };
  showModal.value = true;
  nextTick(() => {
    formRef.value?.restoreValidation();
  });
}

function handleDelete(store: DataStore) {
  const wsName = store.workspaceName || selectedWorkspace.value;
  dialog.warning({
    title: '确认删除',
    content: `确定要删除数据存储 "${store.name}" 吗？相关的图层也会被删除。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await dataStoreApi.delete(wsName, store.name);
        await loadDataStores();
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
    const wsName = editingStore.value.workspaceName!;

    if (isEditing.value) {
      await dataStoreApi.update(wsName, editingStore.value.name!, editingStore.value);
      message.success('更新成功');
    } else {
      await dataStoreApi.create(wsName, editingStore.value);
      message.success('创建成功');
    }
    showModal.value = false;
    await loadDataStores();
    return true;
  } catch {
    message.error('操作失败');
    return false;
  }
}
</script>

<template>
  <div class="p-4">
    <NSpace vertical :size="16">
      <NCard title="数据存储" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
        <template #header-extra>
          <NSpace align="center" justify="end">
            <NSelect
              v-model:value="selectedWorkspace"
              :options="workspaceOptions"
              placeholder="选择工作空间"
              class="w-200px"
              clearable
            />
            <NButton type="primary" @click="handleCreate">
              <template #icon>
                <div class="i-mdi-plus"></div>
              </template>
              添加数据存储
            </NButton>
          </NSpace>
        </template>

        <NDataTable
          v-if="filteredDataStores.length > 0"
          :columns="columns"
          :data="filteredDataStores"
          :loading="loading"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          striped
          class="sm:h-full"
        />
        <NEmpty v-else description="暂无数据存储" />
      </NCard>
    </NSpace>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      :title="isEditing ? '编辑数据存储' : '添加数据存储'"
      :positive-text="isEditing ? '保存' : '创建'"
      negative-text="取消"
      class="w-600px"
      @positive-click="handleSubmit"
    >
      <NForm
        ref="formRef"
        :model="editingStore"
        :rules="rules"
        label-placement="left"
        label-width="100"
        require-mark-placement="right-hanging"
      >
        <NFormItem label="名称" path="name" required>
          <NInput v-model:value="editingStore.name" placeholder="输入数据存储名称" :disabled="isEditing" />
        </NFormItem>
        <NFormItem label="工作空间" path="workspaceName" required>
          <NSelect
            v-model:value="editingStore.workspaceName"
            :options="workspaceOptions"
            placeholder="选择工作空间"
            :disabled="isEditing"
          />
        </NFormItem>
        <NFormItem label="类型" path="type" required>
          <NSelect
            v-model:value="editingStore.type"
            :options="typeOptions"
            placeholder="选择数据类型"
            :disabled="isEditing"
            @update:value="handleTypeChange"
          />
        </NFormItem>

        <!-- Dynamic connection parameters based on type -->
        <template v-if="currentFields.length > 0">
          <div class="section-divider">连接参数</div>
          <NFormItem
            v-for="field in currentFields"
            :key="field.key"
            :label="field.label"
            :path="`connectionParams.${field.key}`"
            :required="field.required"
          >
            <NInput
              v-if="field.type === 'text'"
              v-model:value="(editingStore.connectionParams as Record<string, any>)[field.key]"
              :placeholder="field.placeholder"
            />
            <NInput
              v-else-if="field.type === 'password'"
              v-model:value="(editingStore.connectionParams as Record<string, any>)[field.key]"
              type="password"
              show-password-on="click"
              :placeholder="field.placeholder"
            />
            <NInputNumber
              v-else-if="field.type === 'number'"
              v-model:value="(editingStore.connectionParams as Record<string, any>)[field.key]"
              :placeholder="field.placeholder"
              class="w-full"
            />
            <NCheckbox
              v-else-if="field.type === 'checkbox'"
              :checked="
                (editingStore.connectionParams as Record<string, any>)[field.key] ?? field.defaultValue ?? false
              "
              @update:checked="(editingStore.connectionParams as Record<string, any>)[field.key] = $event"
            >
              {{ field.placeholder || '启用' }}
            </NCheckbox>
            <NInputGroup v-else-if="field.type === 'file'">
              <NInput
                v-model:value="(editingStore.connectionParams as Record<string, any>)[field.key]"
                :placeholder="field.placeholder"
              />
              <NButton @click="openFileSelector(field)">浏览...</NButton>
            </NInputGroup>
          </NFormItem>
        </template>

        <NFormItem label="描述" path="description">
          <NInput v-model:value="editingStore.description" type="textarea" placeholder="输入描述信息" />
        </NFormItem>
        <NFormItem label="启用" path="enabled">
          <NSwitch v-model:value="editingStore.enabled" />
        </NFormItem>
      </NForm>
    </NModal>

    <!-- 文件选择器 -->
    <FileSelector
      v-model:show="showFileSelector"
      title="选择文件"
      :filters="fileSelectorFilters"
      :selectable-type="fileSelectorType"
      :fetch-root-dirs="fetchRootDirs"
      :fetch-dir-content="fetchDirContent"
      @ok="onFileSelected"
    />
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
