package com.xenon.system.service;

import com.xenon.system.domain.vo.FileInfoVo;

import java.util.List;

/**
 * 文件系统服务接口
 *
 * @author charles
 */
public interface ISysFileService {
    /**
     * 获取允许访问的根目录列表
     *
     * @return 根目录列表
     */
    List<FileInfoVo> getRootDirs();

    /**
     * 获取指定目录下的内容
     *
     * @param path 目录路径
     * @return 文件列表
     */
    List<FileInfoVo> listDir(String path);

    /**
     * 验证路径是否在允许范围内
     *
     * @param path 待验证路径
     * @return 是否允许访问
     */
    boolean isPathAllowed(String path);
}
