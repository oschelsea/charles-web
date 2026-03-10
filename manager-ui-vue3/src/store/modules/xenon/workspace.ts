import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { workspaceApi, type Workspace } from '@/service/api/xenon/workspace';

export const useWorkspaceStore = defineStore('workspace', () => {
    // State
    const workspaces = ref<Workspace[]>([]);
    const currentWorkspace = ref<Workspace | null>(null);
    const loading = ref(false);
    const error = ref<string | null>(null);

    // Getters
    const workspaceNames = computed(() => workspaces.value.map(w => w.name));
    const enabledWorkspaces = computed(() => workspaces.value.filter(w => w.enabled));

    // Actions
    async function fetchWorkspaces() {
        loading.value = true;
        error.value = null;
        try {
            const response = await workspaceApi.getAll();
            // Fetch full details for each workspace to get id
            const fullWorkspaces: Workspace[] = [];
            for (const summary of response?.data?.workspaces || []) {
                try {
                    const detail = await workspaceApi.getByName(summary.name);
                    const workspaceData = (detail?.data as any)?.workspace;
                    fullWorkspaces.push(workspaceData);
                } catch {
                    // If detail fetch fails, use summary with just name
                    fullWorkspaces.push({ name: summary.name } as Workspace);
                }
            }
            workspaces.value = fullWorkspaces;
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Failed to fetch workspaces';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function fetchWorkspace(name: string) {
        loading.value = true;
        error.value = null;
        try {
            const response = await workspaceApi.getByName(name);
            const workspaceData = (response?.data as any)?.workspace;
            currentWorkspace.value = workspaceData;
            return workspaceData;
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Failed to fetch workspace';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function createWorkspace(workspace: Partial<Workspace>) {
        loading.value = true;
        error.value = null;
        try {
            const response = await workspaceApi.create(workspace);
            const workspaceData = (response?.data as any)?.workspace;
            if (workspaceData) workspaces.value.push(workspaceData);
            return workspaceData;
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Failed to create workspace';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function updateWorkspace(name: string, workspace: Partial<Workspace>) {
        loading.value = true;
        error.value = null;
        try {
            const response = await workspaceApi.update(name, workspace);
            const index = workspaces.value.findIndex(w => w.name === name);
            const workspaceData = (response?.data as any)?.workspace;
            if (index !== -1 && workspaceData) {
                workspaces.value[index] = workspaceData;
            }
            return workspaceData;
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Failed to update workspace';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function deleteWorkspace(name: string) {
        loading.value = true;
        error.value = null;
        try {
            await workspaceApi.delete(name);
            workspaces.value = workspaces.value.filter(w => w.name !== name);
        } catch (e) {
            error.value = e instanceof Error ? e.message : 'Failed to delete workspace';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    return {
        // State
        workspaces,
        currentWorkspace,
        loading,
        error,
        // Getters
        workspaceNames,
        enabledWorkspaces,
        // Actions
        fetchWorkspaces,
        fetchWorkspace,
        createWorkspace,
        updateWorkspace,
        deleteWorkspace
    };
});
