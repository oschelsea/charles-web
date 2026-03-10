import { request } from '../../request';

export interface Workspace {
    id?: number;
    name: string;
    namespaceUri?: string;
    description?: string;
    isolated?: boolean;
    enabled?: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface WorkspaceSummary {
    name: string;
    href: string;
}

export interface WorkspaceListResponse {
    workspaces: WorkspaceSummary[];
}

export interface WorkspaceResponse {
    workspace: Workspace;
}

export const workspaceApi = {
    /**
     * Get all workspaces
     */
    getAll() {
        return request<WorkspaceListResponse>({
            url: '/api/v1/workspaces',
            method: 'get'
        });
    },

    /**
     * Get a single workspace by name
     */
    getByName(name: string) {
        return request<WorkspaceResponse>({
            url: `/api/v1/workspaces/${name}`,
            method: 'get'
        });
    },

    /**
     * Create a new workspace
     */
    create(workspace: Partial<Workspace>) {
        return request<WorkspaceResponse>({
            url: '/api/v1/workspaces',
            method: 'post',
            data: { workspace }
        });
    },

    /**
     * Update an existing workspace
     */
    update(name: string, workspace: Partial<Workspace>) {
        return request<WorkspaceResponse>({
            url: `/api/v1/workspaces/${name}`,
            method: 'put',
            data: { workspace }
        });
    },

    /**
     * Delete a workspace
     */
    delete(name: string) {
        return request<void>({
            url: `/api/v1/workspaces/${name}`,
            method: 'delete'
        });
    }
};
