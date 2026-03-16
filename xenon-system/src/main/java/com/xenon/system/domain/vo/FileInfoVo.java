package com.xenon.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件信息VO
 *
 * @author charles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件/文件夹名称
     */
    private String name;

    /**
     * 完整路径
     */
    private String path;

    /**
     * 是否为目录（使用 directory 避免布尔字段 is 前缀的序列化问题）
     */
    @JsonProperty("isDir")
    private boolean directory;

    /**
     * 文件大小（字节）
     */
    private long size;

    /**
     * 文件类型/扩展名
     */
    private String type;

    /**
     * 最后修改时间
     */
    private String updateTime;

    /**
     * 是否可读
     */
    private boolean readable;

    /**
     * 是否可写
     */
    private boolean writable;
}
