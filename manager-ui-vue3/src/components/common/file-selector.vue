<script setup>
/**
 * FileSelectorV3 - 通用文件/文件夹选择器组件 (Vue 3 + Naive UI)
 *
 * 使用示例:
 * <FileSelectorV3
 *   v-model:show="showDialog"
 *   title="选择地图文档"
 *   :multiple="false"
 *   :filters="[{ label: '地图文档 (*.mapx)', value: '*.mapx', matchType: 'ext' }]"
 *   selectable-type="file"
 *   :fetch-root-dirs="fetchRootDirs"
 *   :fetch-dir-content="fetchDirContent"
 *   @ok="onFileSelected"
 *   @cancel="onCancel"
 * />
 */
import { computed, h, ref, watch } from 'vue';
import { NIcon } from 'naive-ui';
// 使用 unplugin-icons 导入图标
import IconChevronBack from '~icons/ion/chevron-back';
import IconChevronForward from '~icons/ion/chevron-forward';
import IconCloseOutline from '~icons/ion/close-outline';
import IconDocumentOutline from '~icons/ion/document-outline';
import IconFolderOutline from '~icons/ion/folder-outline';
import IconGridOutline from '~icons/ion/grid-outline';
import IconHelpCircleOutline from '~icons/ion/help-circle-outline';
import IconListOutline from '~icons/ion/list-outline';
import IconSearch from '~icons/ion/search-outline';

const props = defineProps({
  /** 控制弹窗显隐，支持 v-model:show */
  show: { type: Boolean, default: false },
  /** 弹窗标题 */
  title: { type: String, default: '文件选择器' },
  /** 是否开启多选 */
  multiple: { type: Boolean, default: false },
  /**
   * 文件过滤规则数组
   * matchType: 'ext'(后缀) | 'exact'(完整文件名) | 'dir'(仅文件夹) | 'all'(全部)
   */
  filters: {
    type: Array,
    default: () => [{ label: '所有文件 (*.*)', value: '*.*', matchType: 'all' }]
  },
  /** 允许选择的类型: 'file' | 'folder' | 'mixed' */
  selectableType: { type: String, default: 'file' },
  /** 获取根目录列表的异步函数 */
  fetchRootDirs: { type: Function, required: true },
  /** 获取指定路径下内容的异步函数 */
  fetchDirContent: { type: Function, required: true }
});

const emit = defineEmits(['update:show', 'ok', 'cancel']);

// ===================== 状态 =====================
const treeData = ref([]);
const expandedKeys = ref([]);
const selectedTreeKeys = ref([]);
const treeLoading = ref(false);

const currentPath = ref('');
const currentItems = ref([]);
const contentLoading = ref(false);

const historyBack = ref([]);
const historyForward = ref([]);

const selectedItems = ref([]);
const lastClickedIndex = ref(null);

const searchText = ref('');
const currentFilterIndex = ref(0);
const viewMode = ref('list');
const pathInputValue = ref('');
const treePanelWidth = ref(220);

// ===================== 计算属性 =====================
const internalFilters = computed(() =>
  props.filters?.length > 0 ? props.filters : [{ label: '所有文件 (*.*)', value: '*.*', matchType: 'all' }]
);

const currentFilter = computed(() => internalFilters.value[currentFilterIndex.value] || null);

const filteredItems = computed(() => {
  let items = currentItems.value;
  if (currentFilter.value) {
    items = items.filter(matchFilter);
  }
  if (searchText.value) {
    const kw = searchText.value.toLowerCase();
    items = items.filter(item => item.name.toLowerCase().includes(kw));
  }
  return items;
});

const selectedNames = computed(() => {
  if (selectedItems.value.length === 0) return '';
  if (selectedItems.value.length === 1) return selectedItems.value[0].name;
  return selectedItems.value.map(i => `"${i.name}"`).join(' ');
});

const canGoBack = computed(() => historyBack.value.length > 0);
const canGoForward = computed(() => historyForward.value.length > 0);

const canConfirm = computed(() => {
  if (selectedItems.value.length === 0) return false;
  return selectedItems.value.every(item => {
    if (props.selectableType === 'file') return !item.isDir;
    if (props.selectableType === 'folder') return item.isDir;
    return true;
  });
});

const filterOptions = computed(() => internalFilters.value.map((f, i) => ({ label: f.label, value: i })));

// 表格列定义（使用渲染函数）
const columns = computed(() => [
  {
    title: '名称',
    key: 'name',
    ellipsis: { tooltip: true },
    render(row) {
      return h('span', { style: 'display:inline-flex;align-items:center;cursor:pointer' }, [
        h(
          NIcon,
          { size: 16, color: row.isDir ? '#e8a838' : '#8c8c8c', style: 'margin-right:8px;flex-shrink:0' },
          {
            default: () => h(row.isDir ? IconFolderOutline : IconDocumentOutline)
          }
        ),
        row.name
      ]);
    }
  },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: row => (row.isDir ? '文件夹' : row.type || '')
  },
  {
    title: '大小',
    key: 'size',
    width: 100,
    render: row => (row.isDir ? '' : formatSize(row.size))
  },
  { title: '修改日期', key: 'updateTime', width: 180 }
]);

// ===================== 监听 =====================
watch(
  () => props.show,
  val => {
    if (val) init();
    else resetState();
  }
);
watch(
  () => props.filters,
  () => {
    currentFilterIndex.value = 0;
  }
);

// ===================== 初始化 =====================
async function init() {
  treeLoading.value = true;
  try {
    const roots = await props.fetchRootDirs();
    treeData.value = (roots || []).filter(i => i.isDir).map(item => createTreeNode(item, true));

    const lastPath = getLastPath();
    if (lastPath) {
      const ok = await expandToPath(lastPath);
      if (!ok && treeData.value.length > 0) await navigateTo(treeData.value[0].key, false);
    } else if (treeData.value.length > 0) {
      await navigateTo(treeData.value[0].key, false);
      expandedKeys.value = [treeData.value[0].key];
      selectedTreeKeys.value = [treeData.value[0].key];
    }
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('加载根目录失败:', error);
  } finally {
    treeLoading.value = false;
  }
}

function resetState() {
  treeData.value = [];
  expandedKeys.value = [];
  selectedTreeKeys.value = [];
  currentPath.value = '';
  currentItems.value = [];
  historyBack.value = [];
  historyForward.value = [];
  selectedItems.value = [];
  lastClickedIndex.value = null;
  searchText.value = '';
  pathInputValue.value = '';
}

// ===================== 拖拽分割条 =====================
function onSplitterMouseDown(e) {
  e.preventDefault();
  const startX = e.clientX;
  const startWidth = treePanelWidth.value;
  const onMouseMove = ev => {
    const delta = ev.clientX - startX;
    treePanelWidth.value = Math.max(120, Math.min(400, startWidth + delta));
  };
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
  };
  document.body.style.cursor = 'col-resize';
  document.body.style.userSelect = 'none';
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

// ===================== 树操作 =====================
function createTreeNode(item, isRoot = false) {
  // 根节点：如果 name 为空则使用路径最后一部分
  let label = item.name;
  if (isRoot && (!label || label.trim() === '')) {
    const parts = item.path.replace(/\\/g, '/').split('/').filter(Boolean);
    label = parts[parts.length - 1] || item.path;
  }

  return {
    key: normalizePath(item.path),
    label
    // 不设置 isLeaf 和 children，让 NTree 通过 on-load 加载
  };
}

function handleTreeLoad(node) {
  // eslint-disable-next-line no-console
  console.log('[TreeLoad] 触发加载:', node.key);
  return new Promise(resolve => {
    props
      .fetchDirContent(node.key)
      .then(items => {
        // eslint-disable-next-line no-console
        console.log('[TreeLoad] 返回数据:', items);
        const folders = (items || []).filter(i => i.isDir);
        // eslint-disable-next-line no-console
        console.log('[TreeLoad] 文件夹数量:', folders.length);
        // 关键：必须设置children，NTree才会显示子节点
        node.children = folders.map(item => createTreeNode(item, false));
        // eslint-disable-next-line no-console
        console.log('[TreeLoad] 设置children:', node.children);
        if (folders.length === 0) {
          node.isLeaf = true;
        }
        resolve();
      })
      .catch(err => {
        // eslint-disable-next-line no-console
        console.error('[TreeLoad] 错误:', err);
        node.isLeaf = true;
        resolve();
      });
  });
}

function renderTreePrefix() {
  return h(NIcon, { size: 18, color: '#e8a838' }, { default: () => h(IconFolderOutline) });
}

function handleTreeSelectUpdate(keys) {
  if (keys.length === 0) return; // 防止取消选中
  selectedTreeKeys.value = keys;
  navigateTo(keys[0], true);
}

function handleTreeExpandUpdate(keys) {
  expandedKeys.value = keys || [];
}

function findTreeNode(key, nodes) {
  const list = nodes || treeData.value;
  for (const node of list) {
    if (node.key === key) return node;
    if (node.children) {
      const found = findTreeNode(key, node.children);
      if (found) return found;
    }
  }
  return null;
}

// ===================== 导航 =====================
async function navigateTo(path, addToHistory) {
  const np = normalizePath(path);
  if (addToHistory && currentPath.value) {
    historyBack.value.push(currentPath.value);
    historyForward.value = [];
  }
  currentPath.value = np;
  pathInputValue.value = np;
  selectedItems.value = [];
  lastClickedIndex.value = null;
  searchText.value = '';
  selectedTreeKeys.value = [np];

  // 如果允许选择文件夹，导航到任何目录时自动选中当前目录
  if (props.selectableType === 'folder' || props.selectableType === 'mixed') {
    const parts = np.split('/');
    const dirName = parts[parts.length - 1] || np;
    selectedItems.value = [{ name: dirName, path: np, isDir: true, type: '', size: 0, updateTime: '' }];
  }

  contentLoading.value = true;
  try {
    const items = await props.fetchDirContent(np);
    currentItems.value = (items || []).map(item => ({ ...item, path: normalizePath(item.path) }));
    saveLastPath(np);
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('加载目录内容失败:', error);
    currentItems.value = [];
  } finally {
    contentLoading.value = false;
  }
}

async function goBack() {
  if (!canGoBack.value) return;
  const prev = historyBack.value.pop();
  historyForward.value.push(currentPath.value);
  await navigateTo(prev, false);
}
async function goForward() {
  if (!canGoForward.value) return;
  const next = historyForward.value.pop();
  historyBack.value.push(currentPath.value);
  await navigateTo(next, false);
}
async function navigateToInputPath() {
  const p = normalizePath(pathInputValue.value.trim());
  if (p && p !== currentPath.value) {
    await navigateTo(p, true);
    await expandToPath(p);
  }
}

// ===================== 选择操作 =====================
function getRowProps(row, rowIndex) {
  return {
    style: 'cursor: pointer',
    onClick: e => handleItemClick(row, rowIndex, e),
    onDblclick: () => handleItemDblClick(row)
  };
}
function getRowClassName(row) {
  return isSelected(row) ? 'fs-row-selected' : '';
}

function handleItemClick(item, index, event) {
  if (!props.multiple) {
    selectedItems.value = [item];
  } else if (event?.ctrlKey || event?.metaKey) {
    const idx = selectedItems.value.findIndex(s => s.path === item.path);
    if (idx > -1) {
      selectedItems.value = selectedItems.value.filter((_, i) => i !== idx);
    } else {
      selectedItems.value = [...selectedItems.value, item];
    }
  } else if (event?.shiftKey && lastClickedIndex.value !== null) {
    const start = Math.min(lastClickedIndex.value, index);
    const end = Math.max(lastClickedIndex.value, index);
    selectedItems.value = filteredItems.value.slice(start, end + 1);
  } else {
    selectedItems.value = [item];
  }
  lastClickedIndex.value = index;
}

async function handleItemDblClick(item) {
  if (item.isDir) {
    await navigateTo(item.path, true);
    if (!expandedKeys.value.includes(item.path)) {
      expandedKeys.value = [...expandedKeys.value, item.path];
    }
    await ensureTreeNodeLoaded(item.path);
  } else {
    selectedItems.value = [item];
    if (canConfirm.value) handleOk();
  }
}

async function ensureTreeNodeLoaded(path) {
  const node = findTreeNode(path);
  if (node && (!node.children || node.children.length === 0)) {
    try {
      const items = await props.fetchDirContent(path);
      const folders = (items || []).filter(i => i.isDir);
      node.children = folders.map(item => createTreeNode(item, false));
      if (folders.length === 0) node.isLeaf = true;
      treeData.value = [...treeData.value];
    } catch {
      /* 忽略 */
    }
  }
}

function isSelected(item) {
  return selectedItems.value.some(s => s.path === item.path);
}

// ===================== 过滤 =====================
function matchFilter(item) {
  const filter = currentFilter.value;
  if (!filter || filter.matchType === 'all') return true;
  if (item.isDir) return true; // 文件夹始终可见
  switch (filter.matchType) {
    case 'ext': {
      const fv = filter.value.replace(/^\*/, '');
      return item.name.toLowerCase().endsWith(fv.toLowerCase());
    }
    case 'exact':
      return item.name === filter.value;
    case 'dir':
      return false;
    default:
      return true;
  }
}

async function onFilterChange() {
  selectedItems.value = [];
  lastClickedIndex.value = null;
  const lastPath = getLastPath();
  if (lastPath && lastPath !== currentPath.value) {
    await expandToPath(lastPath);
  }
}

// ===================== localStorage =====================
function getStorageKey() {
  const f = currentFilter.value;
  return `fileSelector_lastPath_${f ? f.value : 'default'}`;
}
function saveLastPath(p) {
  try {
    localStorage.setItem(getStorageKey(), p);
  } catch {
    /* 忽略 */
  }
}
function getLastPath() {
  try {
    return localStorage.getItem(getStorageKey());
  } catch {
    return null;
  }
}

async function expandToPath(target) {
  const nt = normalizePath(target);
  const rootNode = treeData.value.find(n => nt === n.key || nt.startsWith(`${n.key}/`));
  if (!rootNode) return false;

  const paths = [rootNode.key];
  if (nt !== rootNode.key) {
    const segs = nt.slice(rootNode.key.length).replace(/^\//, '').split('/').filter(Boolean);
    let cur = rootNode.key;
    for (const seg of segs) {
      cur = `${cur}/${seg}`;
      paths.push(cur);
    }
  }

  /* eslint-disable no-await-in-loop */
  for (const segPath of paths) {
    const node = findTreeNode(segPath);
    if (!node) break;
    if (!node.children || node.children.length === 0) {
      try {
        const items = await props.fetchDirContent(segPath);
        const folders = (items || []).filter(i => i.isDir);
        node.children = folders.map(item => createTreeNode(item, false));
        if (folders.length === 0) node.isLeaf = true;
        treeData.value = [...treeData.value];
      } catch {
        break;
      }
    }
    if (!expandedKeys.value.includes(segPath)) {
      expandedKeys.value = [...expandedKeys.value, segPath];
    }
  }

  await navigateTo(nt, false);
  selectedTreeKeys.value = [nt];
  return true;
}

// ===================== 工具方法 =====================
function normalizePath(p) {
  if (!p) return '';
  return p.replace(/\\/g, '/').replace(/\/+$/, '');
}
function formatSize(bytes) {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Number.parseFloat((bytes / k ** i).toFixed(2)) + sizes[i];
}

// ===================== 弹窗 =====================
function onUpdateShow(val) {
  emit('update:show', val);
}
function handleOk() {
  if (!canConfirm.value) return;
  const result = props.multiple ? selectedItems.value.map(i => ({ ...i })) : { ...selectedItems.value[0] };
  emit('ok', result);
  emit('update:show', false);
}
function handleCancel() {
  emit('cancel');
  emit('update:show', false);
}
</script>

<template>
  <NModal :show="show" :mask-closable="false" :auto-focus="false" draggable @update:show="onUpdateShow">
    <template #default="{ draggableClass }">
      <div class="file-selector-container">
        <!-- 自定义标题栏（拖拽区域） -->
        <div class="fs-header" :class="[draggableClass]">
          <span class="fs-title">{{ title }}</span>
          <div class="fs-header-actions">
            <span class="fs-header-btn">
              <NIcon :size="16"><IconHelpCircleOutline /></NIcon>
            </span>
            <span class="fs-header-btn" @click="handleCancel">
              <NIcon :size="16"><IconCloseOutline /></NIcon>
            </span>
          </div>
        </div>

        <!-- 导航工具栏 -->
        <div class="fs-toolbar">
          <NButton :disabled="!canGoBack" size="small" secondary @click="goBack">
            <NIcon :size="14"><IconChevronBack /></NIcon>
          </NButton>
          <NButton :disabled="!canGoForward" size="small" secondary @click="goForward">
            <NIcon :size="14"><IconChevronForward /></NIcon>
          </NButton>
          <div class="fs-toolbar-flex">
            <NInput v-model:value="pathInputValue" size="small" @keyup.enter="navigateToInputPath" />
          </div>
          <NInput v-model:value="searchText" size="small" class="fs-search-input" placeholder="查找..." clearable />
          <NButton type="primary" size="small" class="fs-search-btn">
            <NIcon :size="14"><IconSearch /></NIcon>
          </NButton>
        </div>

        <!-- 主体区域 -->
        <div class="fs-body">
          <!-- 左侧目录树 -->
          <div class="fs-tree-panel" :style="{ width: treePanelWidth + 'px' }">
            <NSpin :show="treeLoading" size="small">
              <NTree
                :data="treeData"
                :expanded-keys="expandedKeys"
                :selected-keys="selectedTreeKeys"
                :on-load="handleTreeLoad"
                :render-prefix="renderTreePrefix"
                block-line
                cascade
                selectable
                key-field="key"
                label-field="label"
                children-field="children"
                @update:expanded-keys="handleTreeExpandUpdate"
                @update:selected-keys="handleTreeSelectUpdate"
              />
            </NSpin>
          </div>

          <!-- 可拖拽分割条 -->
          <div class="fs-splitter" @mousedown="onSplitterMouseDown"></div>

          <!-- 右侧内容区域 -->
          <div class="fs-content-panel">
            <!-- 视图切换 -->
            <div class="fs-content-toolbar">
              <NRadioGroup v-model:value="viewMode" size="small">
                <NRadioButton value="list">
                  <NIcon :size="18"><IconListOutline /></NIcon>
                </NRadioButton>
                <NRadioButton value="grid">
                  <NIcon :size="18"><IconGridOutline /></NIcon>
                </NRadioButton>
              </NRadioGroup>
            </div>

            <!-- 列表视图 -->
            <div v-if="viewMode === 'list'" class="fs-list-view">
              <NDataTable
                :columns="columns"
                :data="filteredItems"
                :row-props="getRowProps"
                :row-class-name="getRowClassName"
                :row-key="row => row.path"
                :max-height="350"
                :loading="contentLoading"
                size="small"
                :pagination="false"
              />
            </div>

            <!-- 网格视图 -->
            <div v-else class="fs-grid-view">
              <NSpin :show="contentLoading">
                <div class="fs-grid-container">
                  <div
                    v-for="(item, index) in filteredItems"
                    :key="item.path"
                    class="fs-grid-item"
                    :class="[{ 'fs-selected': isSelected(item) }]"
                    @click="handleItemClick(item, index, $event)"
                    @dblclick="handleItemDblClick(item)"
                  >
                    <NIcon v-if="item.isDir" :size="42" color="#e8a838"><IconFolderOutline /></NIcon>
                    <NIcon v-else :size="42" color="#8c8c8c"><IconDocumentOutline /></NIcon>
                    <span class="fs-grid-item-name" :title="item.name">{{ item.name }}</span>
                  </div>
                  <NEmpty
                    v-if="filteredItems.length === 0 && !contentLoading"
                    description="此文件夹为空"
                    class="fs-grid-empty"
                  />
                </div>
              </NSpin>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="fs-footer">
          <div class="fs-footer-row">
            <span class="fs-footer-label">名称:</span>
            <NInput :value="selectedNames" size="small" readonly class="fs-footer-name" />
            <NSelect
              v-model:value="currentFilterIndex"
              :options="filterOptions"
              size="small"
              class="fs-footer-filter"
              @update:value="onFilterChange"
            />
          </div>
          <div class="fs-footer-actions">
            <NButton size="small" @click="handleCancel">取 消</NButton>
            <NButton type="primary" size="small" :disabled="!canConfirm" @click="handleOk">确 定</NButton>
          </div>
        </div>
      </div>
    </template>
  </NModal>
</template>

<style scoped>
.file-selector-container {
  width: 920px;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.15);
}

/* 标题栏 */
.fs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgb(var(--primary-color)), rgb(var(--primary-400-color)));
  color: #fff;
  cursor: move;
}
.fs-title {
  font-size: 15px;
  font-weight: 500;
}
.fs-header-actions {
  display: flex;
  gap: 10px;
}
.fs-header-btn {
  cursor: pointer;
  opacity: 0.85;
  transition: opacity 0.2s;
  display: flex;
  align-items: center;
}
.fs-header-btn:hover {
  opacity: 1;
}

/* 工具栏 */
.fs-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  border-bottom: 1px solid #efeff5;
  background: #fafafc;
}
.fs-toolbar-flex {
  flex: 1;
}
.fs-search-input {
  width: 150px;
}
.fs-search-btn {
  min-width: 32px;
}
.fs-path-input {
  flex: 1;
}
.fs-search-input {
  width: 150px;
}
.fs-search-btn {
  min-width: 32px;
}

/* 主体区域 */
.fs-body {
  display: flex;
  height: 420px;
  border-bottom: 1px solid #efeff5;
}

.fs-tree-panel {
  width: 220px;
  min-width: 120px;
  max-width: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0;
  flex-shrink: 0;
}

.fs-splitter {
  width: 4px;
  cursor: col-resize;
  background: #efeff5;
  flex-shrink: 0;
  transition: background 0.15s;
}
.fs-splitter:hover {
  background: #18a058;
}

.fs-content-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.fs-content-toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 6px 10px;
  border-bottom: 1px solid #f5f5f5;
}

.fs-list-view {
  flex: 1;
  overflow: auto;
}

.fs-grid-view {
  flex: 1;
  overflow: auto;
  padding: 8px;
}
.fs-grid-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-content: flex-start;
}
.fs-grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 90px;
  height: 90px;
  padding: 8px 4px;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s;
}
.fs-grid-item:hover {
  background: #e8f4fd;
}
.fs-grid-item.fs-selected {
  background: #c5e4f7;
}
.fs-grid-item-name {
  margin-top: 4px;
  font-size: 12px;
  max-width: 80px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.fs-grid-empty {
  width: 100%;
  padding-top: 80px;
}

/* 底部 */
.fs-footer {
  padding: 10px 12px;
}
.fs-footer-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.fs-footer-label {
  white-space: nowrap;
  color: #666;
  font-size: 13px;
}
.fs-footer-name {
  flex: 1;
}
.fs-footer-filter {
  width: 180px;
}
.fs-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

<style>
/* 非 scoped — 表格行选中样式 */
.file-selector-container .fs-row-selected td {
  background-color: #e8f4fd !important;
}
.file-selector-container .n-data-table .n-data-table-tr {
  cursor: pointer;
}
.file-selector-container .n-data-table .n-data-table-tr:hover td {
  background-color: #f0f7ff !important;
}
/* 树节点选中样式 */
.file-selector-container .n-tree-node--selected .n-tree-node-content {
  background-color: #e8f4fd !important;
}
/* 视图切换图标居中 */
.file-selector-container .fs-content-toolbar .n-radio-button .n-radio__label {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  height: 28px;
  line-height: 28px;
  box-sizing: border-box;
}
</style>
