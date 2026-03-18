<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSelect,
  NSpace,
  NTabPane,
  NTabs,
  NTag,
  useDialog,
  useMessage
} from 'naive-ui';
import type { DataTableColumns, SelectOption } from 'naive-ui';
import { type Style, type StyleFormat, styleApi, styleFormats } from '@/service/api/xenon/style';

const message = useMessage();
const dialog = useDialog();

const loading = ref(false);
const styles = ref<Style[]>([]);
const searchText = ref('');

const showModal = ref(false);
const showEditorModal = ref(false);
const editingStyle = ref<Partial<Style>>({
  name: '',
  title: '',
  description: '',
  format: 'SLD',
  content: ''
});
const editorContent = ref('');

// Filter styles by search
const filteredStyles = computed(() => {
  if (!searchText.value) return styles.value;
  const search = searchText.value.toLowerCase();
  return styles.value.filter(
    style => style.name.toLowerCase().includes(search) || style.title?.toLowerCase().includes(search)
  );
});

// Format options
const formatOptions = computed<SelectOption[]>(() =>
  Object.entries(styleFormats).map(([value, meta]) => ({
    label: meta.label,
    value
  }))
);

const columns: DataTableColumns<Style> = [
  {
    title: '样式名称',
    key: 'name',
    sorter: 'default',
    render(row) {
      return h('span', { class: 'font-600 text-[var(--n-primary-color)]' }, row.name);
    }
  },
  {
    title: '标题',
    key: 'title',
    ellipsis: { tooltip: true }
  },
  {
    title: '格式',
    key: 'format',
    width: 120,
    render(row) {
      const meta = styleFormats[row.format];
      let tagType: 'info' | 'success' | 'warning' | 'default' = 'default';
      if (row.format === 'SLD') {
        tagType = 'info';
      } else if (row.format === 'CSS') {
        tagType = 'success';
      } else if (row.format === 'YAML' || row.format === 'YSLD') {
        tagType = 'warning';
      }

      return h(
        NTag,
        {
          type: tagType,
          round: true,
          size: 'small'
        },
        { default: () => meta?.label || row.format }
      );
    }
  },
  {
    title: '作用域',
    key: 'workspaceId',
    width: 100,
    render(row) {
      return h(
        NTag,
        {
          type: row.workspaceId ? 'default' : 'info',
          size: 'small'
        },
        { default: () => (row.workspaceId ? '工作空间' : '全局') }
      );
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render(row) {
      return h(
        NSpace,
        { size: 'small' },
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                onClick: () => handleEditContent(row)
              },
              { default: () => '编辑内容' }
            ),
            h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                onClick: () => handleEdit(row)
              },
              { default: () => '属性' }
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
  await loadStyles();
});

async function loadStyles() {
  loading.value = true;
  try {
    const res = await styleApi.getAll();
    styles.value = (res?.data as any) || [];
  } catch {
    message.error('加载样式列表失败');
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  editingStyle.value = {
    name: '',
    title: '',
    description: '',
    format: 'SLD',
    content: getDefaultStyleContent('SLD')
  };
  showModal.value = true;
}

function handleEdit(style: Style) {
  editingStyle.value = { ...style };
  showModal.value = true;
}

function handleEditContent(style: Style) {
  editingStyle.value = { ...style };
  editorContent.value = style.content || getDefaultStyleContent(style.format);
  showEditorModal.value = true;
}

function handleDelete(style: Style) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除样式 "${style.name}" 吗？使用该样式的图层将回退到默认样式。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await styleApi.delete(style.name);
        await loadStyles();
        message.success('删除成功');
      } catch {
        message.error('删除失败');
      }
    }
  });
}

function validateName(name?: string, typeName = '样式') {
  if (!name || !name.trim()) return `${typeName}名称不能为空`;
  if (!/^[a-zA-Z0-9_-]+$/.test(name)) return `${typeName}名称只能包含字母、数字、下划线和连字符`;
  return null;
}

async function handleSubmit() {
  try {
    const error = validateName(editingStyle.value.name, '样式');
    if (error) {
      message.error(error);
      return;
    }

    if (editingStyle.value.id) {
      await styleApi.update(editingStyle.value.name!, editingStyle.value);
      message.success('更新成功');
    } else {
      await styleApi.create(editingStyle.value);
      message.success('创建成功');
    }
    showModal.value = false;
    await loadStyles();
  } catch {
    message.error('操作失败');
  }
}

async function handleSaveContent() {
  try {
    await styleApi.updateContent(editingStyle.value.name!, editorContent.value, editingStyle.value.format!);
    message.success('样式内容已保存');
    showEditorModal.value = false;
    await loadStyles();
  } catch {
    message.error('保存失败');
  }
}

function getDefaultStyleContent(format: StyleFormat): string {
  if (format === 'CSS') {
    return `/* Xenon CSS Style */
* {
  fill: #cccccc;
  stroke: #333333;
  stroke-width: 1;
}
`;
  }
  return `<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0"
  xmlns="http://www.opengis.net/sld"
  xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>default</Name>
    <UserStyle>
      <Title>Default Style</Title>
      <FeatureTypeStyle>
        <Rule>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#cccccc</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke">#333333</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>`;
}
</script>

<template>
  <div class="p-4">
    <NSpace vertical :size="16">
      <NCard title="样式管理" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
        <template #header-extra>
          <NSpace align="center">
            <NInput v-model:value="searchText" placeholder="搜索样式..." clearable class="w-200px" />
            <NButton type="primary" @click="handleCreate">
              <template #icon>
                <div class="i-mdi-plus"></div>
              </template>
              创建样式
            </NButton>
          </NSpace>
        </template>

        <NDataTable
          v-if="filteredStyles.length > 0"
          :columns="columns"
          :data="filteredStyles"
          :loading="loading"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          striped
          class="sm:h-full"
        />
        <NEmpty v-else description="暂无样式定义" />
      </NCard>
    </NSpace>

    <!-- Create/Edit Metadata Modal -->
    <NModal
      v-model:show="showModal"
      preset="dialog"
      :title="editingStyle.id ? '编辑样式属性' : '创建样式'"
      :positive-text="editingStyle.id ? '保存' : '创建'"
      negative-text="取消"
      class="w-500px"
      @positive-click="handleSubmit"
    >
      <NForm :model="editingStyle" label-placement="left" label-width="80" require-mark-placement="right-hanging">
        <NFormItem label="名称" path="name" required>
          <NInput v-model:value="editingStyle.name" placeholder="输入样式名称" :disabled="!!editingStyle.id" />
        </NFormItem>
        <NFormItem label="标题" path="title">
          <NInput v-model:value="editingStyle.title" placeholder="输入显示标题" />
        </NFormItem>
        <NFormItem label="格式" path="format" required>
          <NSelect v-model:value="editingStyle.format" :options="formatOptions" placeholder="选择样式格式" />
        </NFormItem>
        <NFormItem label="描述" path="description">
          <NInput v-model:value="editingStyle.description" type="textarea" placeholder="输入描述信息" />
        </NFormItem>
      </NForm>
    </NModal>

    <!-- Style Editor Modal -->
    <NModal
      v-model:show="showEditorModal"
      preset="card"
      :title="`编辑样式: ${editingStyle.name}`"
      class="h-80vh max-w-1000px w-80%"
    >
      <div class="style-editor">
        <NTabs type="line" animated>
          <NTabPane name="editor" tab="编辑器">
            <div class="editor-container mt-4">
              <textarea v-model="editorContent" class="code-editor" spellcheck="false" />
            </div>
          </NTabPane>
          <NTabPane name="preview" tab="预览">
            <div class="preview-container">
              <p>样式预览功能开发中...</p>
            </div>
          </NTabPane>
        </NTabs>
      </div>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showEditorModal = false">取消</NButton>
          <NButton type="primary" @click="handleSaveContent">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.card-wrapper {
  height: calc(100vh - 120px);
}

.style-editor {
  height: calc(80vh - 150px);
}

.editor-container {
  height: 100%;
}

.code-editor {
  width: 100%;
  height: 480px;
  padding: 16px;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background: var(--n-color-modal, #1e1e24);
  color: var(--n-text-color, #e6e6e6);
  border: 1px solid var(--n-border-color);
  border-radius: 8px;
  resize: none;
}

.code-editor:focus {
  outline: none;
  border-color: var(--n-primary-color);
}

.preview-container {
  padding: 24px;
  text-align: center;
  color: var(--n-text-color-3);
}
</style>
