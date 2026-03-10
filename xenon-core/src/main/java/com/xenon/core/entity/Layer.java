package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xenon.core.enums.LayerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Layer entity - a published resource that can be served via OGC services.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName("layer")
public class Layer extends BaseEntity {

    @NotBlank
    private String name;

    private String title;

    private String description;

    @NotNull
    private LayerType type;

    @TableField("is_enabled")
    private Boolean enabled = true;

    @TableField("is_advertised")
    private Boolean advertised = true;

    @TableField("is_queryable")
    private Boolean queryable = true;

    @TableField("is_opaque")
    private Boolean opaque = false;

    private Long featureTypeId;

    private Long coverageId;

    private Long datastoreId;

    private Long workspaceId;

    private Long defaultStyleId;

    private String srs;

    /**
     * Get the qualified name (workspace:name).
     */
    public String getQualifiedName(String workspaceName) {
        if (workspaceName != null) {
            return workspaceName + ":" + name;
        }
        return name;
    }
}
