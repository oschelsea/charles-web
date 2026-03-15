package com.xenon.project.system.controller;

import com.xenon.admin.service.SysLoginService;
import com.xenon.admin.service.SysPermissionService;
import com.xenon.common.core.domain.R;
import com.xenon.common.core.domain.entity.SysMenu;
import com.xenon.common.core.domain.entity.SysUser;
import com.xenon.framework.security.LoginBody;
import com.xenon.framework.security.SecurityUtils;
import com.xenon.system.domain.vo.RouterVo;
import com.xenon.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 登录验证
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
public class SysLoginController {
    private final SysLoginService loginService;
    private final ISysMenuService menuService;
    private final SysPermissionService permissionService;

    public record LoginInfoVo(String token) {
    }

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public R<LoginInfoVo> login(@RequestBody LoginBody loginBody) {
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        return R.ok(new LoginInfoVo(token));
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public R<UserInfoVo> getInfo() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        return R.ok(new UserInfoVo(user, roles, permissions));
    }

    public record UserInfoVo(SysUser user, Set<String> roles, Set<String> permissions) {
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public R<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return R.ok(menuService.buildMenus(menus));
    }
}
