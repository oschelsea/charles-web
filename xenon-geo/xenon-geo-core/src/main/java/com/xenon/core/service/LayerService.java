package com.xenon.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xenon.core.entity.Layer;
import com.xenon.core.exception.ResourceNotFoundException;
import com.xenon.core.mapper.LayerMapper;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Service for managing layers.
 */
@Slf4j
@Service
public class LayerService extends ServiceImpl<LayerMapper, Layer> {

    @org.springframework.beans.factory.annotation.Autowired
    private WorkspaceService workspaceService;

    /**
     * Get all layers.
     */
    public List<Layer> findAll() {
        return list();
    }

    /**
     * Get all advertised layers.
     */
    public List<Layer> findAdvertised() {
        return baseMapper.selectAdvertised();
    }

    /**
     * Find layers by data store IDs.
     */
    public List<Layer> findByDataStoreIds(List<Long> dataStoreIds) {
        if (dataStoreIds == null || dataStoreIds.isEmpty()) {
            return Collections.emptyList();
        }
        return list(new QueryWrapper<Layer>().in("datastore_id", dataStoreIds));
    }

    /**
     * Find a layer by name.
     */
    public Layer findByName(String name) {
        Layer layer = baseMapper.selectByName(name);
        if (layer == null) {
            throw new ResourceNotFoundException("Layer", name);
        }
        return layer;
    }

    /**
     * Find a layer by name and workspace name.
     */
    public Layer findByNameAndWorkspace(String layerName, String workspaceName) {
        com.xenon.core.entity.Workspace workspace = workspaceService.findByName(workspaceName);
        Layer layer = baseMapper.selectOne(new QueryWrapper<Layer>()
                .eq("name", layerName)
                .eq("workspace_id", workspace.getId())
                .eq("deleted", 0));

        if (layer == null) {
            throw new ResourceNotFoundException("Layer", workspaceName + ":" + layerName);
        }
        return layer;
    }

    /**
     * Create (publish) a new layer.
     */
    @Transactional
    public Layer create(Layer layer) {
        log.info("Publishing layer: {}", layer.getName());

        // Check if there's a soft-deleted layer with the same name and physically delete it
        Layer deletedLayer = baseMapper.selectDeletedByName(layer.getName());
        if (deletedLayer != null) {
            log.info("Found soft-deleted layer with same name, physically deleting: {}", layer.getName());
            baseMapper.deleteById(deletedLayer.getId());
        }

        save(layer);
        return layer;
    }

    /**
     * Update a layer.
     */
    @Transactional
    public Layer update(String name, Layer updates) {
        Layer existing = findByName(name);

        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getEnabled() != null) {
            existing.setEnabled(updates.getEnabled());
        }
        if (updates.getAdvertised() != null) {
            existing.setAdvertised(updates.getAdvertised());
        }
        if (updates.getQueryable() != null) {
            existing.setQueryable(updates.getQueryable());
        }
        if (updates.getDefaultStyleId() != null) {
            existing.setDefaultStyleId(updates.getDefaultStyleId());
        }

        log.info("Updating layer: {}", name);
        updateById(existing);
        return existing;
    }

    /**
     * Delete a layer.
     */
    @Transactional
    public void delete(String name) {
        Layer layer = findByName(name);
        log.info("Deleting layer: {}", name);
        removeById(layer.getId());
    }
}
