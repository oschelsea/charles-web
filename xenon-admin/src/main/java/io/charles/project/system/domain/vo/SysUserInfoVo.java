package io.charles.project.system.domain.vo;

import io.charles.project.system.domain.SysPost;
import io.charles.project.system.domain.SysRole;
import io.charles.project.system.domain.SysUser;
import lombok.Data;

import java.util.List;

/**
 * Created on 2026/2/12.
 *
 * @author Chelsea
 */
@Data
public class SysUserInfoVo {
    /**
     * 用户信息
     */
    private SysUser user;

    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;

    /**
     * 角色列表
     */
    private List<SysRole> roles;

    /**
     * 岗位ID列表
     */
    private List<Integer> postIds;

    /**
     * 岗位列表
     */
    private List<SysPost> posts;
}
