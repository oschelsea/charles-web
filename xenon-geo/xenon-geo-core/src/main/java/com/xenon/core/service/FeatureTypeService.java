package com.xenon.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xenon.core.entity.FeatureType;
import com.xenon.core.exception.ResourceNotFoundException;
import com.xenon.core.mapper.FeatureTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing feature types.
 */
@Service
public class FeatureTypeService extends ServiceImpl<FeatureTypeMapper, FeatureType> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeatureTypeService.class);

    /**
     * Get all feature types in a datastore.
     */
    public List<FeatureType> findByDatastoreId(Long datastoreId) {
        return baseMapper.selectByDatastoreId(datastoreId);
    }

    /**
     * Find a feature type by datastore and name.
     */
    public FeatureType findByDatastoreAndName(Long datastoreId, String name) {
        FeatureType featureType = baseMapper.selectByDatastoreIdAndName(datastoreId, name);
        if (featureType == null) {
            throw new ResourceNotFoundException("FeatureType", name);
        }
        return featureType;
    }

    /**
     * Find a feature type by name.
     */
    public FeatureType findByName(String name) {
        FeatureType featureType = baseMapper.selectByName(name);
        if (featureType == null) {
            throw new ResourceNotFoundException("FeatureType", name);
        }
        return featureType;
    }

    /**
     * Create (publish) a new feature type.
     */
    @Transactional
    public FeatureType create(FeatureType featureType) {
        LOG.info("Publishing feature type: {} in datastore {}", 
                featureType.getName(), featureType.getDatastoreId());
        
        // Set defaults
        if (featureType.getSrs() == null) {
            featureType.setSrs("EPSG:4326");
        }
        if (featureType.getEnabled() == null) {
            featureType.setEnabled(true);
        }
        
        save(featureType);
        return featureType;
    }

    /**
     * Update a feature type.
     */
    @Transactional
    public FeatureType update(Long datastoreId, String name, FeatureType updates) {
        FeatureType existing = findByDatastoreAndName(datastoreId, name);
        
        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getSrs() != null) {
            existing.setSrs(updates.getSrs());
        }
        if (updates.getEnabled() != null) {
            existing.setEnabled(updates.getEnabled());
        }
        
        LOG.info("Updating feature type: {}", name);
        updateById(existing);
        return existing;
    }

    /**
     * Delete a feature type.
     */
    @Transactional
    public void delete(Long datastoreId, String name) {
        FeatureType featureType = findByDatastoreAndName(datastoreId, name);
        LOG.info("Deleting feature type: {}", name);
        removeById(featureType.getId());
    }
}
