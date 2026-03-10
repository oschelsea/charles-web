package com.xenon.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.core.entity.Layer;
import com.xenon.core.enums.LayerType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper for Layer entities.
 */
@Mapper
public interface LayerMapper extends BaseMapper<Layer> {

    /**
     * Find a layer by name.
     */
    @Select("SELECT * FROM layer WHERE name = #{name} AND deleted = 0")
    Layer selectByName(@Param("name") String name);

    /**
     * Find all enabled and advertised layers.
     */
    @Select("SELECT * FROM layer WHERE is_enabled = 1 AND is_advertised = 1 AND deleted = 0")
    List<Layer> selectAdvertised();

    /**
     * Find layers by type.
     */
    @Select("SELECT * FROM layer WHERE type = #{type} AND deleted = 0")
    List<Layer> selectByType(@Param("type") LayerType type);

    /**
     * Find a soft-deleted layer by name.
     */
    @Select("SELECT * FROM layer WHERE name = #{name} AND deleted = 1")
    Layer selectDeletedByName(@Param("name") String name);
}
