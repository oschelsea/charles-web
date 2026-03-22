import { request } from '../../request';

export interface Style {
  id?: number;
  name: string;
  title?: string;
  description?: string;
  format: StyleFormat;
  content?: string;
  filename?: string;
  workspaceId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export type StyleFormat = 'SLD' | 'SLD_1_1' | 'CSS' | 'MBSTYLE' | 'YSLD';

export interface StyleSummary {
  name: string;
  href: string;
}

export interface StyleListResponse {
  styles: StyleSummary[];
}

export interface StyleResponse {
  style: Style;
}

export const styleApi = {
  /**
   * Get all global styles
   */
  getAll() {
    return request<StyleListResponse>({
      url: '/api/v1/styles',
      method: 'get'
    });
  },

  /**
   * Get styles in a workspace
   */
  getByWorkspace(workspaceName: string) {
    return request<StyleListResponse>({
      url: `/api/v1/workspaces/${workspaceName}/styles`,
      method: 'get'
    });
  },

  /**
   * Get a single style
   */
  getByName(styleName: string) {
    return request<StyleResponse>({
      url: `/api/v1/styles/${styleName}`,
      method: 'get'
    });
  },

  /**
   * Get style content (SLD/CSS)
   */
  getContent(styleName: string) {
    return request<string>({
      url: `/api/v1/styles/${styleName}/content`,
      method: 'get',
      headers: { Accept: 'text/plain' }
    });
  },

  /**
   * Create a new style
   */
  create(style: Partial<Style>) {
    return request<StyleResponse>({
      url: '/api/v1/styles',
      method: 'post',
      data: { style }
    });
  },

  /**
   * Update style metadata
   */
  update(styleName: string, style: Partial<Style>) {
    return request<StyleResponse>({
      url: `/api/v1/styles/${styleName}`,
      method: 'put',
      data: { style }
    });
  },

  /**
   * Update style content
   */
  updateContent(styleName: string, content: string, format: StyleFormat) {
    return request<void>({
      url: `/api/v1/styles/${styleName}/content`,
      method: 'put',
      data: content,
      headers: {
        'Content-Type': format === 'CSS' ? 'text/css' : 'application/vnd.ogc.sld+xml'
      }
    });
  },

  /**
   * Delete a style
   */
  delete(styleName: string) {
    return request<void>({
      url: `/api/v1/styles/${styleName}`,
      method: 'delete'
    });
  }
};

// Style format metadata
export const styleFormats: Record<StyleFormat, { label: string; extension: string; mimeType: string }> = {
  SLD: { label: 'SLD 1.0', extension: '.sld', mimeType: 'application/vnd.ogc.sld+xml' },
  SLD_1_1: { label: 'SLD 1.1', extension: '.sld', mimeType: 'application/vnd.ogc.se+xml' },
  CSS: { label: 'CSS', extension: '.css', mimeType: 'text/css' },
  MBSTYLE: { label: 'Mapbox Style', extension: '.json', mimeType: 'application/json' },
  YSLD: { label: 'YSLD', extension: '.ysld', mimeType: 'application/yaml' }
};
