package com.xenon.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xenon.common.constant.UserConstants;
import com.xenon.framework.security.SecurityUtils;
import com.xenon.common.utils.StringUtils;
import com.xenon.common.utils.poi.ExcelUtil;
import com.xenon.common.annotation.Log;
import com.xenon.common.enums.BusinessType;
import com.xenon.framework.web.controller.BaseController;
import com.xenon.common.core.domain.R;
import com.xenon.common.core.domain.TreeSelect;
import com.xenon.framework.web.page.TableDataInfo;
import com.xenon.common.core.domain.entity.SysDept;
import com.xenon.common.core.domain.entity.SysRole;
import com.xenon.common.core.domain.entity.SysUser;
import com.xenon.system.domain.vo.SysUserInfoVo;
import com.xenon.system.service.ISysDeptService;
import com.xenon.system.service.ISysPostService;
import com.xenon.system.service.ISysRoleService;
import com.xenon.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPostService postService;
    private final ISysDeptService deptService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        Page<SysUser> page = getPage();
        userService.selectUserList(page, user);
        return getDataTable(page);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(SysUser user, HttpServletResponse response) {
        List<SysUser> list = userService.selectUserList(null, user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public R<Void> importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return R.ok(message);
    }

    @GetMapping("/importTemplate")
    public R<Void> importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public R<SysUserInfoVo> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        List<SysRole> roles = roleService.selectRoleAll();
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        userInfoVo.setPosts(postService.selectPostAll());
        userInfoVo.setRoles(SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (userId != null) {
            userInfoVo.setUser(userService.selectUserById(userId));
            userInfoVo.setPostIds(postService.selectPostListByUserId(userId));
            userInfoVo.setRoleIds(roleService.selectRoleListByUserId(userId));
        }
        return R.ok(userInfoVo);
    }


    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toResult(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return toResult(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            return R.fail("当前用户不能删除");
        }
        return toResult(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toResult(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setUpdateBy(getUsername());
        return toResult(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public R<SysUserInfoVo> authRole(@PathVariable("userId") Long userId) {
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        userInfoVo.setUser(user);
        userInfoVo.setRoles(SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return R.ok(userInfoVo);
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public R<Void> insertAuthRole(Long userId, Long[] roleIds) {
        userService.insertUserAuth(userId, roleIds);
        return R.ok();
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public R<List<TreeSelect>> deptTree(SysDept dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }

    /**
     * 获取部门下的所有用户信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list/dept/{deptId}")
    public R<List<SysUser>> listByDept(@PathVariable @NotNull Long deptId) {
        return R.ok(userService.selectUserListByDept(deptId));
    }
}
