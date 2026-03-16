package com.xenon.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件系统访问配置
 *
 * @author charles
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.browser")
public class FileProperties {
    /**
     * 允许访问的根目录列表
     */
    private List<String> allowedRoots = new ArrayList<>();

    /**
     * 是否启用文件浏览
     */
    private boolean enabled = true;
}
