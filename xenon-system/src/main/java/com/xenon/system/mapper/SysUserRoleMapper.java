package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.system.domain.SysUserRole;
import org.apache.ibatis.executor.BatchResult;

import java.util.Arrays;
import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author charles
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 通过用户ID删除用户和角色关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    default int deleteUserRoleByUserId(Long userId) {
        return delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
    }

    /**
     * 批量删除用户和角色关联
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteUserRole(Long[] ids) {
        return delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, Arrays.asList(ids)));
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int countUserRoleByRoleId(Long roleId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)));
    }

    /**
     * 批量新增用户角色信息
     *
     * @param userRoleList 用户角色列表
     * @return 结果
     */
    default int batchUserRole(List<SysUserRole> userRoleList) {
        List<BatchResult> results = insert(userRoleList);
        return results.stream().map(t -> Arrays.stream(t.getUpdateCounts()).sum()).mapToInt(Integer::intValue).sum();
    }

    /**
     * 删除用户和角色关联信息
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    default int deleteUserRoleInfo(SysUserRole userRole) {
        return delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userRole.getUserId())
                .eq(SysUserRole::getRoleId, userRole.getRoleId()));
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    default int deleteUserRoleInfos(Long roleId, Long[] userIds) {
        return delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, Arrays.asList(userIds)));
    }
}
