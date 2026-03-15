package com.xenon.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xenon.common.constant.UserConstants;
import com.xenon.common.utils.poi.ExcelUtil;
import com.xenon.common.annotation.Log;
import com.xenon.common.enums.BusinessType;
import com.xenon.framework.web.controller.BaseController;
import com.xenon.common.core.domain.R;
import com.xenon.common.core.domain.TreeSelect;
import com.xenon.framework.web.page.TableDataInfo;
import com.xenon.common.core.domain.entity.SysDept;
import com.xenon.system.domain.SysPost;
import com.xenon.system.service.ISysDeptService;
import com.xenon.system.service.ISysPostService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位信息操作处理
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {
    private final ISysPostService postService;
    private final ISysDeptService deptService;

    /**
     * 获取岗位列表
     */
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysPost post) {
        Page<SysPost> page = getPage();
        postService.selectPostList(page, post);
        return getDataTable(page);
    }

    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:post:export')")
    @PostMapping("/export")
    public void export(SysPost post, HttpServletResponse response) {
        List<SysPost> list = postService.selectPostList(post);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        util.exportExcel(response, list, "岗位数据");
    }

    /**
     * 根据岗位编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @GetMapping(value = "/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        return R.ok(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysPost post) {
        if (UserConstants.NOT_UNIQUE.equals(postService.checkPostNameUnique(post))) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(postService.checkPostCodeUnique(post))) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setCreateBy(getUsername());
        return toResult(postService.insertPost(post));
    }

    /**
     * 修改岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysPost post) {
        if (UserConstants.NOT_UNIQUE.equals(postService.checkPostNameUnique(post))) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(postService.checkPostCodeUnique(post))) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        post.setUpdateBy(getUsername());
        return toResult(postService.updatePost(post));
    }

    /**
     * 删除岗位
     */
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable Long[] postIds) {
        return toResult(postService.deletePostByIds(postIds));
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    public R<List<SysPost>> optionselect() {
        List<SysPost> posts = postService.selectPostAll();
        return R.ok(posts);
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @GetMapping("/deptTree")
    public R<List<TreeSelect>> deptTree(SysDept dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }
}
