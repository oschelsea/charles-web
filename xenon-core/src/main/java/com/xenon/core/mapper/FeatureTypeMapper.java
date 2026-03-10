package com.xenon.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.core.entity.FeatureType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis-Plus mapper for FeatureType entity.
 */
@Mapper
public interface FeatureTypeMapper extends BaseMapper<FeatureType> {

    /**
     * Find all feature types by datastore ID.
     */
    @Select("SELECT * FROM feature_type WHERE datastore_id = #{datastoreId} AND deleted = 0")
    List<FeatureType> selectByDatastoreId(@Param("datastoreId") Long datastoreId);

    /**
     * Find a feature type by datastore ID and name.
     */
    @Select("SELECT * FROM feature_type WHERE datastore_id = #{datastoreId} AND name = #{name} AND deleted = 0")
    FeatureType selectByDatastoreIdAndName(@Param("datastoreId") Long datastoreId, @Param("name") String name);

    /**
     * Find a feature type by name.
     */
    @Select("SELECT * FROM feature_type WHERE name = #{name} AND deleted = 0")
    FeatureType selectByName(@Param("name") String name);
}
