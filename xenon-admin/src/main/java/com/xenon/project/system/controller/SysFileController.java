package com.xenon.project.system.controller;

import com.xenon.common.core.domain.R;
import com.xenon.framework.web.controller.BaseController;
import com.xenon.system.domain.vo.FileInfoVo;
import com.xenon.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文件系统控制器
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/system/file")
public class SysFileController extends BaseController {

    private final ISysFileService fileService;

    /**
     * 获取允许访问的根目录列表
     *
     * @return 根目录列表
     */
    @GetMapping("/roots")
    public R<List<FileInfoVo>> getRoots() {
        return R.ok(fileService.getRootDirs());
    }

    /**
     * 获取指定目录下的内容
     *
     * @param path 目录路径
     * @return 文件列表
     */
    @GetMapping("/list")
    public R<List<FileInfoVo>> listDir(@RequestParam(required = false) String path) {
        return R.ok(fileService.listDir(path));
    }
}
