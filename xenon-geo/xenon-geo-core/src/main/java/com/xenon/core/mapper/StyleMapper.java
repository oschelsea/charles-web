package com.xenon.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.core.entity.Style;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper for Style entities.
 */
@Mapper
public interface StyleMapper extends BaseMapper<Style> {

    /**
     * Find a global style by name.
     */
    @Select("SELECT * FROM style WHERE name = #{name} AND workspace_id IS NULL AND deleted = 0")
    Style selectGlobalByName(@Param("name") String name);

    /**
     * Find a style by workspace ID and name.
     */
    @Select("SELECT * FROM style WHERE workspace_id = #{workspaceId} AND name = #{name} AND deleted = 0")
    Style selectByWorkspaceIdAndName(@Param("workspaceId") Long workspaceId, @Param("name") String name);

    /**
     * Find all global styles.
     */
    @Select("SELECT * FROM style WHERE workspace_id IS NULL AND deleted = 0")
    List<Style> selectAllGlobal();

    /**
     * Find all styles in a workspace.
     */
    @Select("SELECT * FROM style WHERE workspace_id = #{workspaceId} AND deleted = 0")
    List<Style> selectByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
