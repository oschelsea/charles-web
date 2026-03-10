package com.xenon.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class with common fields for all entities.
 */
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * Logical delete flag (0=not deleted, 1=deleted)
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @lombok.Builder.Default
    private Integer deleted = 0;
}
