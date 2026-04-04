package com.xenon.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xenon.core.entity.Style;
import com.xenon.core.entity.Workspace;
import com.xenon.common.exception.ResourceAlreadyExistsException;
import com.xenon.common.exception.ResourceNotFoundException;
import com.xenon.core.mapper.StyleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing styles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StyleService extends ServiceImpl<StyleMapper, Style> {

    private final WorkspaceService workspaceService;

    public List<Style> findAllGlobal() {
        return baseMapper.selectAllGlobal();
    }

    public List<Style> findByWorkspace(String workspaceName) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        return baseMapper.selectByWorkspaceId(workspace.getId());
    }

    public Style findGlobalByName(String name) {
        Style style = baseMapper.selectGlobalByName(name);
        if (style == null) {
            throw new ResourceNotFoundException("Global Style", name);
        }
        return style;
    }

    public Style findByName(String name) {
        return findGlobalByName(name); // Simply proxy to global for now as UI requests /api/v1/styles/{name}
    }

    @Transactional
    public Style create(Style style) {
        if (style.isGlobal() && baseMapper.selectGlobalByName(style.getName()) != null) {
            throw new ResourceAlreadyExistsException("Global Style", style.getName());
        }
        log.info("Creating style: {}", style.getName());
        save(style);
        return style;
    }

    @Transactional
    public Style update(String name, Style updates) {
        Style existing = findGlobalByName(name);
        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getFormat() != null) {
            existing.setFormat(updates.getFormat());
        }
        if (updates.getContent() != null) {
            existing.setContent(updates.getContent());
        }
        log.info("Updating style: {}", name);
        updateById(existing);
        return existing;
    }
    
    @Transactional
    public void updateContent(String name, String content) {
        Style existing = findGlobalByName(name);
        existing.setContent(content);
        log.info("Updating style content: {}", name);
        updateById(existing);
    }

    @Transactional
    public void delete(String name) {
        Style style = findGlobalByName(name);
        log.info("Deleting style: {}", name);
        removeById(style.getId());
    }
}
