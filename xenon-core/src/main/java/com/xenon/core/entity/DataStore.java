package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xenon.core.enums.DataStoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * DataStore entity - represents a connection to a data source.
 * Can be a PostGIS database, Shapefile directory, GeoPackage, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName(value = "datastore", autoResultMap = true)
public class DataStore extends BaseEntity {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private DataStoreType type;

    @TableField("is_enabled")
    private Boolean enabled = true;

    /**
     * Connection parameters stored as JSON.
     * For PostGIS: host, port, database, schema, user, password
     * For Shapefile: url (file path)
     * For GeoPackage: database (file path)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> connectionParams = new HashMap<>();

    private Long workspaceId;

    /**
     * Get a connection parameter value.
     */
    public Object getConnectionParam(String key) {
        return connectionParams.get(key);
    }

    /**
     * Set a connection parameter value.
     */
    public void setConnectionParam(String key, Object value) {
        connectionParams.put(key, value);
    }
}
