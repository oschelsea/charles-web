import { request } from '../../request';

export interface Layer {
  id?: number;
  name: string;
  title?: string;
  description?: string;
  type: LayerType;
  enabled?: boolean;
  advertised?: boolean;
  queryable?: boolean;
  opaque?: boolean;
  featureTypeId?: number;
  coverageId?: number;
  datastoreId?: number;
  datastoreName?: string;
  workspaceId?: number;
  workspaceName?: string;
  defaultStyleId?: number;
  srs?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type LayerType = 'VECTOR' | 'RASTER' | 'GROUP' | 'WMS' | 'TILES3D' | 'ARCGIS_CACHE' | 'GEOPACKAGE_TILES';

export interface LayerSummary {
  name: string;
  href: string;
}

export interface LayerListResponse {
  layers: LayerSummary[];
}

export interface LayerResponse {
  layer: Layer;
}

export const layerApi = {
  /**
   * Get all layers
   */
  getAll() {
    return request<LayerListResponse>({
      url: '/api/v1/layers',
      method: 'get'
    });
  },

  /**
   * Get layers in a workspace
   */
  getByWorkspace(workspaceName: string) {
    return request<LayerListResponse>({
      url: `/api/v1/workspaces/${workspaceName}/layers`,
      method: 'get'
    });
  },

  /**
   * Get a single layer
   */
  getByName(layerName: string) {
    return request<LayerResponse>({
      url: `/api/v1/layers/${layerName}`,
      method: 'get'
    });
  },

  /**
   * Create/publish a layer
   */
  create(layer: Partial<Layer>) {
    return request<LayerResponse>({
      url: '/api/v1/layers',
      method: 'post',
      data: { layer }
    });
  },

  /**
   * Update a layer
   */
  update(layerName: string, layer: Partial<Layer>) {
    return request<LayerResponse>({
      url: `/api/v1/layers/${layerName}`,
      method: 'put',
      data: { layer }
    });
  },

  /**
   * Delete a layer
   */
  delete(layerName: string) {
    return request<any>({
      url: `/api/v1/layers/${layerName}`,
      method: 'delete'
    });
  }
};

// Layer type metadata
export const layerTypes: Record<LayerType, { label: string; icon: string; color: string }> = {
  VECTOR: { label: '矢量图层', icon: '📐', color: '#36d1dc' },
  RASTER: { label: '栅格图层', icon: '🖼️', color: '#5b86e5' },
  GROUP: { label: '图层组', icon: '📁', color: '#f093fb' },
  WMS: { label: 'WMS级联', icon: '🌐', color: '#f5576c' },
  TILES3D: { label: '3DTiles', icon: '🏗️', color: '#00d4aa' },
  ARCGIS_CACHE: { label: 'ArcGIS Cache', icon: '🗺️', color: '#ff9f43' },
  GEOPACKAGE_TILES: { label: 'GeoPackage瓦片', icon: '📦', color: '#00b894' }
};

// Publishable resource from a data store
export interface PublishableResource {
  name: string; // Resource name (table name / file name)
  nativeName: string; // Native name in the data source
  type: 'vector' | 'raster' | 'tiles3d' | 'arcgiscache';
  title?: string;
}

export interface PublishableResourcesResponse {
  resources: PublishableResource[];
}

// Get publishable resources from a data store
export function getPublishableResources(workspaceName: string, datastoreName: string, isVector = true) {
  const resourceType = isVector ? 'featuretypes' : 'coverages';
  // Call real API to get available (unpublished) resources from datastore
  return request<any>({
    url: `/api/v1/workspaces/${workspaceName}/datastores/${datastoreName}/${resourceType}?list=available`,
    method: 'get'
  })
    .then((response: any) => {
      // Transform API response to PublishableResource format
      const key = isVector ? 'featureTypes' : 'coverages';
      const resources: PublishableResource[] = (response.data[key] || []).map((res: any) => ({
        name: res.name,
        nativeName: res.name,
        type: isVector ? 'vector' : 'raster',
        title: res.title || res.name
      }));
      return { resources };
    })
    .catch(() => {
      // If API fails, return empty list
      return { resources: [] };
    });
}
