package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Workspace entity - a container for datastores, layers, and styles.
 * Similar to GeoServer's workspace concept.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName("workspace")
public class Workspace extends BaseEntity {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "Workspace name must start with a letter")
    private String name;

    private String namespaceUri;

    private String description;

    @TableField("is_isolated")
    private Boolean isolated = false;

    @TableField("is_enabled")
    private Boolean enabled = true;
}
