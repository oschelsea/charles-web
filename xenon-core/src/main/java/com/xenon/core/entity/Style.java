package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xenon.core.enums.StyleFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Style entity - represents a layer style definition.
 * Supports SLD (Styled Layer Descriptor) and CSS formats.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName("style")
public class Style extends BaseEntity {

    @NotBlank
    private String name;

    private String title;

    private String description;

    @NotNull
    private StyleFormat format = StyleFormat.SLD;

    /**
     * The style content (SLD XML or CSS)
     */
    @TableField("content")
    private String content;

    private String filename;

    private Long workspaceId;

    /**
     * Get the qualified name (workspace:name or just name for global styles).
     */
    public String getQualifiedName(String workspaceName) {
        if (workspaceName != null) {
            return workspaceName + ":" + name;
        }
        return name;
    }

    public boolean isGlobal() {
        return workspaceId == null;
    }
}
