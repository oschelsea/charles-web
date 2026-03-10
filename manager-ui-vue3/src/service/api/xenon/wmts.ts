import { request } from '../../request';

export interface WmtsLayerInfo {
    name: string;
    title: string;
    description?: string;
    type?: string;
    enabled: boolean;
    tileUrl: string;
    formats: string[];
    tileMatrixSets: string[];
}

export interface TileMatrixSetInfo {
    identifier: string;
    supportedCRS: string;
    minZoom: number;
    maxZoom: number;
    tileWidth: number;
    tileHeight: number;
}

export interface WmtsServiceInfo {
    title: string;
    description: string;
    version: string;
    capabilitiesUrl: string;
    layers: WmtsLayerInfo[];
    tileMatrixSets: TileMatrixSetInfo[];
}

export interface WmtsLayerListResponse {
    layers: WmtsLayerInfo[];
}

export interface WmtsLayerResponse {
    layer: WmtsLayerInfo;
}

// Ensure proxy path routes correctly. Use '/dev-api/xenon' matching VITE_APP_BASE_API.
const CONTEXT_PATH = '/dev-api/xenon';

export const wmtsApi = {
    /**
     * Get WMTS service information and capabilities
     */
    getCapabilities() {
        return request<WmtsServiceInfo>({
            url: '/xenon/wmts/capabilities',
            method: 'get'
        });
    },

    /**
     * Get all WMTS layers
     */
    getLayers() {
        return request<WmtsLayerListResponse>({
            url: '/xenon/wmts/layers',
            method: 'get'
        });
    },

    /**
     * Get a single WMTS layer
     */
    getLayerByName(name: string) {
        return request<WmtsLayerResponse>({
            url: `/xenon/wmts/layers/${name}`,
            method: 'get'
        });
    }
};

/**
 * 构建 WMTS 瓦片 URL（用于 Leaflet）
 * @param qualifiedLayerName 限定图层名 (workspace:layerName)
 * @param options 选项
 */
export function buildWmtsTileUrl(
    qualifiedLayerName: string,
    options?: {
        tileMatrixSet?: string;
        format?: string;
        baseUrl?: string;
    }
): string {
    // baseUrl is usually empty, defaulting to same-origin for proxied tile requests
    const baseUrl = options?.baseUrl || '';
    const contextPath = CONTEXT_PATH;
    const tileMatrixSet = options?.tileMatrixSet || 'EPSG:3857';
    const format = options?.format || 'png';

    // 新 URL 格式: /dev-api/xenon/services/{workspace}:{layerName}/wmts/{tileMatrixSet}/{z}/{x}/{y}.{format}
    return `${baseUrl}${contextPath}/services/${qualifiedLayerName}/wmts/${tileMatrixSet}/{z}/{x}/{y}.${format}`;
}

/**
 * 构建 WMTS GetCapabilities URL
 * @param qualifiedLayerName 限定图层名 (workspace:layerName)
 * @param baseUrl 基础 URL
 */
export function buildCapabilitiesUrl(qualifiedLayerName: string, baseUrl?: string): string {
    const base = baseUrl || '';
    const contextPath = CONTEXT_PATH;
    return `${base}${contextPath}/services/${qualifiedLayerName}/wmts?SERVICE=WMTS&REQUEST=GetCapabilities`;
}

/**
 * 构建服务基础 URL
 * @param qualifiedLayerName 限定图层名 (workspace:layerName) 
 * @param serviceType 服务类型 (wms, wmts, 3dtiles)
 */
export function buildServiceUrl(qualifiedLayerName: string, serviceType: string): string {
    return `${''}${CONTEXT_PATH}/services/${qualifiedLayerName}/${serviceType}`;
}
