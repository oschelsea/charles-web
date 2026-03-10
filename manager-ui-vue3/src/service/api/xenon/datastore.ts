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
        return request<void>({
            url: `/api/v1/workspaces/${workspaceName}/datastores/${datastoreName}`,
            method: 'delete'
        });
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
    ARCGIS_CACHE: { label: 'ArcGIS Cache', icon: '🗺️', isVector: false }
};

// Field configuration for connection parameters
export interface FieldConfig {
    key: string;
    label: string;
    type: 'text' | 'password' | 'number' | 'file';
    required?: boolean;
    placeholder?: string;
    defaultValue?: string | number;
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
        { key: 'url', label: '文件路径', type: 'text', required: true, placeholder: '/path/to/file.shp 或 file:///C:/data/file.shp' }
    ],
    GEOPACKAGE: [
        { key: 'database', label: '文件路径', type: 'text', required: true, placeholder: '/path/to/file.gpkg' }
    ],
    GEOJSON: [
        { key: 'url', label: '文件路径/URL', type: 'text', required: true, placeholder: '/path/to/file.geojson 或 http://...' }
    ],
    GEOTIFF: [
        { key: 'url', label: '文件路径', type: 'text', required: true, placeholder: '/path/to/file.tif' }
    ],
    IMAGE_MOSAIC: [
        { key: 'url', label: '目录路径', type: 'text', required: true, placeholder: '/path/to/mosaic/directory' }
    ],
    WMS: [
        { key: 'GET_CAPABILITIES_URL', label: '服务URL', type: 'text', required: true, placeholder: 'http://...?service=WMS&request=GetCapabilities' },
        { key: 'user', label: '用户名', type: 'text', placeholder: '可选' },
        { key: 'passwd', label: '密码', type: 'password', placeholder: '可选' }
    ],
    WFS: [
        { key: 'GET_CAPABILITIES_URL', label: '服务URL', type: 'text', required: true, placeholder: 'http://...?service=WFS&request=GetCapabilities' },
        { key: 'user', label: '用户名', type: 'text', placeholder: '可选' },
        { key: 'passwd', label: '密码', type: 'password', placeholder: '可选' }
    ],
    TILES3D_CACHE: [
        { key: 'tilesetPath', label: 'tileset.json路径', type: 'text', required: true, placeholder: '/path/to/tileset.json 或 C:\\data\\tiles\\tileset.json' }
    ],
    ARCGIS_CACHE: [
        { key: 'path', label: '缓存目录路径', type: 'text', required: true, placeholder: '/path/to/cache (包含 conf.xml) 或 C:\\arcgiscache\\Services\\Map\\Layers' }
    ]
};
