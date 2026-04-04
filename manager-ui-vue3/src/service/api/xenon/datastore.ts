import { request } from '../../request';

export interface DataStore {
  id?: number;
  name: string;
  description?: string;
  type: DataStoreType;
  enabled?: boolean;
  connectionParams?: Record<string, unknown>;
  workspaceId: number;
  workspaceName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type DataStoreType =
  | 'POSTGIS'
  | 'SHAPEFILE'
  | 'GEOPACKAGE'
  | 'GEOJSON'
  | 'GEOTIFF'
  | 'IMAGE_MOSAIC'
  | 'WMS'
  | 'WFS'
  | 'TILES3D_CACHE'
  | 'TERRAIN_CACHE'
  | 'ARCGIS_CACHE';

export interface DataStoreSummary {
  name: string;
  href: string;
}

export interface DataStoreListResponse {
  dataStores: DataStoreSummary[];
}

export interface DataStoreResponse {
  dataStore: DataStore;
}

export const dataStoreApi = {
  /**
   * Get all datastores across all workspaces (with details)
   */
  getAll() {
    return request<DataStore[]>({
      url: '/api/v1/datastores/detail',
      method: 'get'
    });
  },

  /**
   * Get all datastores in a workspace
   */
  getByWorkspace(workspaceName: string) {
    return request<DataStoreListResponse>({
      url: `/api/v1/workspaces/${workspaceName}/datastores`,
      method: 'get'
    });
  },

  /**
   * Get a single datastore
   */
  getByName(workspaceName: string, datastoreName: string) {
    return request<DataStoreResponse>({
      url: `/api/v1/workspaces/${workspaceName}/datastores/${datastoreName}`,
      method: 'get'
    });
  },

  /**
   * Create a new datastore
   */
  create(workspaceName: string, dataStore: Partial<DataStore>) {
    return request<DataStoreResponse>({
      url: `/api/v1/workspaces/${workspaceName}/datastores`,
      method: 'post',
      data: { dataStore }
    });
  },

  /**
   * Update a datastore
   */
  update(workspaceName: string, datastoreName: string, dataStore: Partial<DataStore>) {
    return request<DataStoreResponse>({
      url: `/api/v1/workspaces/${workspaceName}/datastores/${datastoreName}`,
      method: 'put',
      data: { dataStore }
    });
  },

  /**
   * Delete a datastore
   */
  delete(workspaceName: string, datastoreName: string) {
    return request({
      url: `/api/v1/workspaces/${workspaceName}/datastores/${datastoreName}`,
      method: 'delete'
    });
  },

  /**
   * Get terrain metadata from meta.json
   * Used to auto-fill zipped parameter when creating TERRAIN_CACHE datastore
   */
  async getTerrainMeta(layerJsonPath: string) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const contextPath = import.meta.env.VITE_CONTEXT_PATH || '/xenon';
    const url = `${baseUrl}${contextPath}/services/terrain/meta?layerJsonPath=${encodeURIComponent(layerJsonPath)}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch terrain meta: ${response.status}`);
    }

    return response.json();
  }
};

// Data store type metadata
export const dataStoreTypes: Record<DataStoreType, { label: string; icon: string; isVector: boolean }> = {
  POSTGIS: { label: 'PostGIS', icon: '🐘', isVector: true },
  SHAPEFILE: { label: 'Shapefile', icon: '📄', isVector: true },
  GEOPACKAGE: { label: 'GeoPackage', icon: '📦', isVector: true },
  GEOJSON: { label: 'GeoJSON', icon: '📋', isVector: true },
  GEOTIFF: { label: 'GeoTIFF', icon: '🖼️', isVector: false },
  IMAGE_MOSAIC: { label: 'Image Mosaic', icon: '🗺️', isVector: false },
  WMS: { label: 'WMS (Cascading)', icon: '🌐', isVector: false },
  WFS: { label: 'WFS (Cascading)', icon: '🔗', isVector: true },
  TILES3D_CACHE: { label: '3DTiles', icon: '🏗️', isVector: false },
  TERRAIN_CACHE: { label: 'Terrain Cache', icon: '🏔️', isVector: false },
  ARCGIS_CACHE: { label: 'ArcGIS Cache', icon: '🗺️', isVector: false }
};

// Field configuration for connection parameters
/**
 * 文件过滤器配置
 */
export interface FileFilter {
  label: string;
  value: string;
  matchType: 'ext' | 'exact' | 'dir' | 'all';
}

export interface FieldConfig {
  key: string;
  label: string;
  type: 'text' | 'password' | 'number' | 'file' | 'checkbox';
  required?: boolean;
  placeholder?: string;
  defaultValue?: string | number | boolean;
  /** 文件过滤器配置（仅 type='file' 时有效） */
  fileFilter?: FileFilter;
  /** 选择类型：file(文件) | folder(文件夹) | mixed(混合) */
  selectType?: 'file' | 'folder' | 'mixed';
}

// Connection parameter fields for each data store type
export const dataStoreFieldConfigs: Record<DataStoreType, FieldConfig[]> = {
  POSTGIS: [
    { key: 'host', label: '主机', type: 'text', required: true, placeholder: 'localhost', defaultValue: 'localhost' },
    { key: 'port', label: '端口', type: 'number', required: true, placeholder: '5432', defaultValue: 5432 },
    { key: 'database', label: '数据库', type: 'text', required: true, placeholder: '数据库名' },
    { key: 'schema', label: 'Schema', type: 'text', placeholder: 'public', defaultValue: 'public' },
    { key: 'user', label: '用户名', type: 'text', required: true, placeholder: '用户名' },
    { key: 'passwd', label: '密码', type: 'password', required: true, placeholder: '密码' }
  ],
  SHAPEFILE: [
    {
      key: 'url',
      label: '文件路径',
      type: 'file',
      required: true,
      placeholder: '选择 .shp 文件',
      selectType: 'file',
      fileFilter: { label: 'Shapefile (*.shp)', value: '*.shp', matchType: 'ext' }
    }
  ],
  GEOPACKAGE: [
    {
      key: 'database',
      label: '文件路径',
      type: 'file',
      required: true,
      placeholder: '选择 .gpkg 文件',
      selectType: 'file',
      fileFilter: { label: 'GeoPackage (*.gpkg)', value: '*.gpkg', matchType: 'ext' }
    }
  ],
  GEOJSON: [
    {
      key: 'url',
      label: '文件路径/URL',
      type: 'file',
      required: true,
      placeholder: '选择 GeoJSON 文件或输入 URL',
      selectType: 'file',
      fileFilter: { label: 'GeoJSON (*.geojson, *.json)', value: '*.geojson', matchType: 'ext' }
    }
  ],
  GEOTIFF: [
    {
      key: 'url',
      label: '文件路径',
      type: 'file',
      required: true,
      placeholder: '选择 GeoTIFF 文件',
      selectType: 'file',
      fileFilter: { label: 'GeoTIFF (*.tif, *.tiff)', value: '*.tif', matchType: 'ext' }
    }
  ],
  IMAGE_MOSAIC: [
    {
      key: 'url',
      label: '目录路径',
      type: 'file',
      required: true,
      placeholder: '选择影像目录',
      selectType: 'folder'
    }
  ],
  WMS: [
    {
      key: 'GET_CAPABILITIES_URL',
      label: '服务URL',
      type: 'text',
      required: true,
      placeholder: 'http://...?service=WMS&request=GetCapabilities'
    },
    { key: 'user', label: '用户名', type: 'text', placeholder: '可选' },
    { key: 'passwd', label: '密码', type: 'password', placeholder: '可选' }
  ],
  WFS: [
    {
      key: 'GET_CAPABILITIES_URL',
      label: '服务URL',
      type: 'text',
      required: true,
      placeholder: 'http://...?service=WFS&request=GetCapabilities'
    },
    { key: 'user', label: '用户名', type: 'text', placeholder: '可选' },
    { key: 'passwd', label: '密码', type: 'password', placeholder: '可选' }
  ],
  TILES3D_CACHE: [
    {
      key: 'tilesetPath',
      label: 'tileset.json路径',
      type: 'file',
      required: true,
      placeholder: '选择 tileset.json 文件',
      selectType: 'file',
      fileFilter: { label: '3D Tiles (tileset.json)', value: 'tileset.json', matchType: 'exact' }
    }
  ],
  TERRAIN_CACHE: [
    {
      key: 'layerJsonPath',
      label: 'layer.json路径',
      type: 'file',
      required: true,
      placeholder: '选择 layer.json 文件',
      selectType: 'file',
      fileFilter: { label: 'Terrain (layer.json)', value: 'layer.json', matchType: 'exact' }
    },
    {
      key: 'zipped',
      label: 'Gzip压缩',
      type: 'checkbox',
      defaultValue: false,
      placeholder: '地形瓦片是否使用gzip压缩'
    }
  ],
  ARCGIS_CACHE: [
    {
      key: 'path',
      label: '缓存配置文件路径',
      type: 'file',
      required: true,
      placeholder: '选择 ArcGIS 缓存配置文件Conf.xml',
      selectType: 'file',
      fileFilter: { label: 'ArcGIS Tile(Conf.xml)', value: 'Conf.xml', matchType: 'exact' }
    }
  ]
};
