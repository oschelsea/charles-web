package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * FeatureType entity - represents a vector layer resource from a data store.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName("feature_type")
public class FeatureType extends BaseEntity {

    @NotBlank
    private String name;

    private String nativeName;

    private String title;

    private String description;

    private String keywords;

    /**
     * Spatial Reference System identifier (e.g., "EPSG:4326")
     */
    private String srs;

    private String nativeSrs;

    private Double bboxMinX;
    private Double bboxMinY;
    private Double bboxMaxX;
    private Double bboxMaxY;

    private Double nativeBboxMinX;
    private Double nativeBboxMinY;
    private Double nativeBboxMaxX;
    private Double nativeBboxMaxY;

    @TableField("is_enabled")
    private Boolean enabled = true;

    @NotNull
    private Long datastoreId;

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
