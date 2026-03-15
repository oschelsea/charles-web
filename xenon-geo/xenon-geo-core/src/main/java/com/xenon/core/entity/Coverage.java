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
 * Coverage entity - represents a raster layer resource from a data store.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@TableName("coverage")
public class Coverage extends BaseEntity {

    @NotBlank
    private String name;

    private String nativeName;

    private String title;

    private String description;

    private String keywords;

    private String srs;

    private String nativeSrs;

    private String nativeFormat;

    private Double bboxMinX;
    private Double bboxMinY;
    private Double bboxMaxX;
    private Double bboxMaxY;

    private Integer width;
    private Integer height;
    private Integer numBands;

    @TableField("is_enabled")
    private Boolean enabled = true;

    @NotNull
    private Long datastoreId;

    public String getQualifiedName(String workspaceName) {
        if (workspaceName != null) {
            return workspaceName + ":" + name;
        }
        return name;
    }
}
