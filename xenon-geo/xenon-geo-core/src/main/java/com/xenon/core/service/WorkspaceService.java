package com.xenon.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xenon.core.entity.Workspace;
import com.xenon.core.exception.ResourceAlreadyExistsException;
import com.xenon.core.exception.ResourceNotFoundException;
import com.xenon.core.mapper.WorkspaceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing workspaces.
 */
@Slf4j
@Service
public class WorkspaceService extends ServiceImpl<WorkspaceMapper, Workspace> {

    /**
     * Get all workspaces.
     */
    public List<Workspace> findAll() {
        return list();
    }

    /**
     * Get all enabled workspaces.
     */
    public List<Workspace> findAllEnabled() {
        return baseMapper.selectEnabled();
    }

    /**
     * Find a workspace by name.
     */
    public Workspace findByName(String name) {
        Workspace workspace = baseMapper.selectByName(name);
        if (workspace == null) {
            throw new ResourceNotFoundException("Workspace", name);
        }
        return workspace;
    }

    /**
     * Find a workspace by ID.
     */
    public Workspace findById(Long id) {
        return getById(id);
    }

    /**
     * Check if a workspace exists.
     */
    public boolean exists(String name) {
        return baseMapper.existsByName(name);
    }

    /**
     * Create a new workspace.
     */
    @Transactional
    public Workspace create(Workspace workspace) {
        if (baseMapper.existsByName(workspace.getName())) {
            throw new ResourceAlreadyExistsException("Workspace", workspace.getName());
        }
        
        // Set default namespace URI if not provided
        if (workspace.getNamespaceUri() == null) {
            workspace.setNamespaceUri("http://xenon.com/" + workspace.getName());
        }
        
        log.info("Creating workspace: {}", workspace.getName());
        save(workspace);
        return workspace;
    }

    /**
     * Update an existing workspace.
     */
    @Transactional
    public Workspace update(String name, Workspace updates) {
        Workspace existing = findByName(name);
        
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getNamespaceUri() != null) {
            existing.setNamespaceUri(updates.getNamespaceUri());
        }
        if (updates.getIsolated() != null) {
            existing.setIsolated(updates.getIsolated());
        }
        if (updates.getEnabled() != null) {
            existing.setEnabled(updates.getEnabled());
        }
        
        log.info("Updating workspace: {}", name);
        updateById(existing);
        return existing;
    }

    /**
     * Delete a workspace by name.
     */
    @Transactional
    public void delete(String name) {
        Workspace workspace = findByName(name);
        log.info("Deleting workspace: {}", name);
        removeById(workspace.getId());
    }
}
