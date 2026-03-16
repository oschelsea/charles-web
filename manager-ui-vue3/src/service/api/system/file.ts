import { request } from '../../request';

/**
 * 文件信息接口
 */
export interface FileInfo {
  name: string;
  path: string;
  isDir: boolean;
  size: number;
  type: string;
  updateTime: string;
  readable: boolean;
  writable: boolean;
}

/**
 * 文件系统 API
 */
export const fileApi = {
  /**
   * 获取根目录列表
   */
  getRoots() {
    return request<FileInfo[]>({
      url: '/system/file/roots',
      method: 'get'
    });
  },

  /**
   * 获取目录内容
   * @param path 目录路径
   */
  listDir(path: string) {
    return request<FileInfo[]>({
      url: '/system/file/list',
      method: 'get',
      params: { path }
    });
  }
};

/**
 * FileSelector 需要的获取根目录函数
 */
export function fetchRootDirs(): Promise<FileInfo[]> {
  return fileApi.getRoots().then(res => res?.data || []);
}

/**
 * FileSelector 需要的获取目录内容函数
 */
export function fetchDirContent(path: string): Promise<FileInfo[]> {
  return fileApi.listDir(path).then(res => res?.data || []);
}
