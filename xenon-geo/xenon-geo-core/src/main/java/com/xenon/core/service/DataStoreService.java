package com.xenon.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xenon.core.entity.DataStore;
import com.xenon.core.exception.ResourceNotFoundException;
import com.xenon.core.mapper.DataStoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing data stores.
 */
@Slf4j
@Service
public class DataStoreService extends ServiceImpl<DataStoreMapper, DataStore> {

    /**
     * Get all data stores in a workspace.
     */
    public List<DataStore> findByWorkspaceId(Long workspaceId) {
        return baseMapper.selectByWorkspaceId(workspaceId);
    }

    /**
     * Find a data store by ID.
     */
    public DataStore findById(Long id) {
        return getById(id);
    }

    /**
     * Find a data store by workspace and name.
     */
    public DataStore findByWorkspaceAndName(Long workspaceId, String name) {
        DataStore dataStore = baseMapper.selectByWorkspaceIdAndName(workspaceId, name);
        if (dataStore == null) {
            throw new ResourceNotFoundException("DataStore", name);
        }
        return dataStore;
    }

    /**
     * Create a new data store.
     */
    @Transactional
    public DataStore create(DataStore dataStore) {
        log.info("Creating datastore: {} in workspace {}", dataStore.getName(), dataStore.getWorkspaceId());
        // Ensure logical delete flag is set to 0 (not deleted)
        if (dataStore.getDeleted() == null) {
            dataStore.setDeleted(0);
        }
        save(dataStore);
        return dataStore;
    }

    /**
     * Update a data store.
     */
    @Transactional
    public DataStore update(Long workspaceId, String name, DataStore updates) {
        DataStore existing = findByWorkspaceAndName(workspaceId, name);
        
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getEnabled() != null) {
            existing.setEnabled(updates.getEnabled());
        }
        if (updates.getConnectionParams() != null) {
            existing.setConnectionParams(updates.getConnectionParams());
        }
        
        log.info("Updating datastore: {}", name);
        updateById(existing);
        return existing;
    }

    /**
     * Delete a data store.
     */
    @Transactional
    public void delete(Long workspaceId, String name) {
        DataStore dataStore = findByWorkspaceAndName(workspaceId, name);
        log.info("Deleting datastore: {}", name);
        removeById(dataStore.getId());
    }
}
