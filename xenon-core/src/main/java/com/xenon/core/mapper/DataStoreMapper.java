package com.xenon.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xenon.core.entity.DataStore;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Mapper for DataStore entities.
 */
@Mapper
public interface DataStoreMapper extends BaseMapper<DataStore> {

    /**
     * Find all datastores in a workspace.
     */
    @Select("SELECT * FROM datastore WHERE workspace_id = #{workspaceId} AND deleted = 0")
    @Results(id = "dataStoreResult", value = {
        @Result(column = "id", property = "id"),
        @Result(column = "name", property = "name"),
        @Result(column = "description", property = "description"),
        @Result(column = "type", property = "type"),
        @Result(column = "is_enabled", property = "enabled"),
        @Result(column = "connection_params", property = "connectionParams", 
                typeHandler = JacksonTypeHandler.class, javaType = Map.class),
        @Result(column = "workspace_id", property = "workspaceId"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "updated_at", property = "updatedAt"),
        @Result(column = "created_by", property = "createdBy"),
        @Result(column = "updated_by", property = "updatedBy"),
        @Result(column = "deleted", property = "deleted")
    })
    List<DataStore> selectByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * Find a datastore by workspace ID and name.
     */
    @Select("SELECT * FROM datastore WHERE workspace_id = #{workspaceId} AND name = #{name} AND deleted = 0")
    @ResultMap("dataStoreResult")
    DataStore selectByWorkspaceIdAndName(@Param("workspaceId") Long workspaceId, @Param("name") String name);
}

