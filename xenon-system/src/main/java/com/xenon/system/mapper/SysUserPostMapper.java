package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.system.domain.SysUserPost;
import org.apache.ibatis.executor.BatchResult;

import java.util.Arrays;
import java.util.List;

/**
 * 用户与岗位关联表 数据层
 *
 * @author charles
 */
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
    /**
     * 通过用户ID删除用户和岗位关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    default int deleteUserPostByUserId(Long userId) {
        return delete(new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getUserId, userId));
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    default int countUserPostById(Long postId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getPostId, postId)));
    }

    /**
     * 批量删除用户和岗位关联
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteUserPost(Long[] ids) {
        return delete(new LambdaQueryWrapper<SysUserPost>()
                .in(SysUserPost::getUserId, Arrays.asList(ids)));
    }

    /**
     * 批量新增用户岗位信息
     *
     * @param userPostList 用户角色列表
     * @return 结果
     */
    default int batchUserPost(List<SysUserPost> userPostList) {
        List<BatchResult> results = insert(userPostList);
        return results.stream().map(t -> Arrays.stream(t.getUpdateCounts()).sum()).mapToInt(Integer::intValue).sum();
    }
}
