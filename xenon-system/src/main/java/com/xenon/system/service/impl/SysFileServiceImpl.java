package com.xenon.system.service.impl;

import com.xenon.common.exception.ServiceException;
import com.xenon.common.utils.StringUtils;
import com.xenon.system.config.FileProperties;
import com.xenon.system.domain.vo.FileInfoVo;
import com.xenon.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件系统服务实现
 *
 * @author charles
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl implements ISysFileService {

    private final FileProperties fileProperties;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<FileInfoVo> getRootDirs() {
        if (!fileProperties.isEnabled()) {
            throw new ServiceException("文件浏览功能未启用");
        }

        List<String> allowedRoots = fileProperties.getAllowedRoots();
        if (allowedRoots == null || allowedRoots.isEmpty()) {
            // 默认返回系统根目录
            File[] roots = File.listRoots();
            return Arrays.stream(roots)
                    .map(this::toFileInfo)
                    .toList();
        }

        List<FileInfoVo> result = new ArrayList<>();
        for (String rootPath : allowedRoots) {
            File dir = new File(rootPath);
            if (dir.exists() && dir.isDirectory()) {
                result.add(toFileInfo(dir));
            }
        }
        return result;
    }

    @Override
    public List<FileInfoVo> listDir(String path) {
        if (!fileProperties.isEnabled()) {
            throw new ServiceException("文件浏览功能未启用");
        }

        if (!isPathAllowed(path)) {
            throw new ServiceException("无权访问该路径");
        }

        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new ServiceException("目录不存在或不是有效目录");
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        // 先文件夹后文件，按名称排序
        return Arrays.stream(files)
                .sorted((a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .map(this::toFileInfo)
                .toList();
    }

    @Override
    public boolean isPathAllowed(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }

        List<String> allowedRoots = fileProperties.getAllowedRoots();
        if (allowedRoots == null || allowedRoots.isEmpty()) {
            // 未配置白名单时允许所有路径
            return true;
        }

        try {
            Path normalizedPath = Paths.get(path).toAbsolutePath().normalize();

            // 检查路径遍历攻击
            String pathStr = normalizedPath.toString();
            if (path.contains("..") || pathStr.contains("..")) {
                log.warn("检测到可疑路径访问: {}", path);
                return false;
            }

            // 检查是否在允许的根目录下
            for (String root : allowedRoots) {
                Path rootPath = Paths.get(root).toAbsolutePath().normalize();
                if (normalizedPath.startsWith(rootPath)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("路径验证失败: {}", path, e);
            return false;
        }
    }

    private FileInfoVo toFileInfo(File file) {
        FileInfoVo vo = new FileInfoVo();
        vo.setName(file.getName());
        vo.setPath(file.getAbsolutePath());
        vo.setDirectory(file.isDirectory());
        vo.setSize(file.isDirectory() ? 0 : file.length());
        vo.setType(file.isDirectory() ? "" : getExtension(file.getName()));
        vo.setUpdateTime(dateFormat.format(file.lastModified()));
        vo.setReadable(file.canRead());
        vo.setWritable(file.canWrite());

        // Windows 下根目录名称处理：显示盘符如 "C:" 而非完整路径
        if (file.isAbsolute() && file.getParent() == null && file.isDirectory()) {
            String path = file.getAbsolutePath();
            // 提取盘符部分，如 "C:\\" -> "C:"
            vo.setName(path.substring(0, path.length() - 1));
        }

        return vo;
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(idx + 1).toLowerCase() : "";
    }
}
