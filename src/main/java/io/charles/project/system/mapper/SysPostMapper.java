package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.StringUtils;
import io.charles.project.system.domain.SysPost;

import java.util.Arrays;
import java.util.List;

/**
 * 岗位信息 数据层
 *
 * @author charles
 */
public interface SysPostMapper extends BaseMapper<SysPost> {
    /**
     * 查询岗位数据集合
     *
     * @param post 岗位信息
     * @return 岗位数据集合
     */
    default List<SysPost> selectPostList(SysPost post) {
        return selectPostList(null, post);
    }

    /**
     * 查询岗位数据集合
     *
     * @param page 分页对象
     * @param post 岗位信息
     * @return 岗位数据集合
     */
    default List<SysPost> selectPostList(IPage<SysPost> page, SysPost post) {
        LambdaQueryWrapper<SysPost> wrapper = Wrappers.lambdaQuery();
        if (post != null) {
            wrapper.like(StringUtils.isNotEmpty(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
                    .eq(StringUtils.isNotEmpty(post.getStatus()), SysPost::getStatus, post.getStatus())
                    .like(StringUtils.isNotEmpty(post.getPostName()), SysPost::getPostName, post.getPostName());
        }
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    default List<SysPost> selectPostAll() {
        return selectList(null);
    }

    /**
     * 通过岗位ID查询岗位信息
     *
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    default SysPost selectPostById(Long postId) {
        return selectById(postId);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Integer> selectPostListByUserId(Long userId);

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    List<SysPost> selectPostsByUserName(String userName);

    /**
     * 删除岗位信息
     *
     * @param postId 岗位ID
     * @return 结果
     */
    default int deletePostById(Long postId) {
        return deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    default int deletePostByIds(Long[] postIds) {
        return deleteBatchIds(Arrays.asList(postIds));
    }

    /**
     * 修改岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    default int updatePost(SysPost post) {
        return updateById(post);
    }

    /**
     * 新增岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    default int insertPost(SysPost post) {
        return insert(post);
    }

    /**
     * 校验岗位名称
     *
     * @param postName 岗位名称
     * @return 结果
     */
    default SysPost checkPostNameUnique(String postName) {
        return selectOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostName, postName)
                .last("limit 1"));
    }

    /**
     * 校验岗位编码
     *
     * @param postCode 岗位编码
     * @return 结果
     */
    default SysPost checkPostCodeUnique(String postCode) {
        return selectOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostCode, postCode)
                .last("limit 1"));
    }
}
