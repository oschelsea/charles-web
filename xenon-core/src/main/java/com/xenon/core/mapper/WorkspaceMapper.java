package com.xenon.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.core.entity.Workspace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper for Workspace entities.
 */
@Mapper
public interface WorkspaceMapper extends BaseMapper<Workspace> {

    /**
     * Find a workspace by name.
     */
    @Select("SELECT * FROM workspace WHERE name = #{name} AND deleted = 0")
    Workspace selectByName(String name);

    /**
     * Find all enabled workspaces.
     */
    @Select("SELECT * FROM workspace WHERE is_enabled = 1 AND deleted = 0")
    List<Workspace> selectEnabled();

    /**
     * Check if a workspace exists by name.
     */
    @Select("SELECT COUNT(*) > 0 FROM workspace WHERE name = #{name} AND deleted = 0")
    boolean existsByName(String name);
}
