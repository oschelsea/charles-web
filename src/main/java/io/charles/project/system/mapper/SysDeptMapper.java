package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.StringUtils;
import io.charles.project.system.domain.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 部门管理 数据层
 *
 * @author charles
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    default List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDept::getDelFlag, "0");
        if (dept != null) {
            wrapper.eq(dept.getDeptId() != null && dept.getDeptId() != 0, SysDept::getDeptId, dept.getDeptId())
                    .eq(dept.getParentId() != null && dept.getParentId() != 0, SysDept::getParentId, dept.getParentId())
                    .like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                    .eq(StringUtils.isNotEmpty(dept.getStatus()), SysDept::getStatus, dept.getStatus());

            // 数据范围过滤
            Map<String, Object> params = dept.getParams();
            if (params != null && params.get("dataScope") != null) {
                wrapper.apply(params.get("dataScope").toString());
            }
        }
        wrapper.orderByAsc(SysDept::getParentId, SysDept::getOrderNum);
        return selectList(wrapper);
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Integer> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    default SysDept selectDeptById(Long deptId) {
        return selectById(deptId);
    }

    /**
     * 根据ID查询所有子部门
     *
     * @param deptId 部门ID
     * @return 部门列表
     */
    default List<SysDept> selectChildrenDeptById(Long deptId) {
        return selectList(new LambdaQueryWrapper<SysDept>()
                .and(w -> w.eq(SysDept::getAncestors, String.valueOf(deptId))
                        .or().likeRight(SysDept::getAncestors, deptId + ",")
                        .or().likeLeft(SysDept::getAncestors, "," + deptId)
                        .or().like(SysDept::getAncestors, "," + deptId + ",")));
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    default int selectNormalChildrenDeptById(Long deptId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, "0")
                .eq(SysDept::getDelFlag, "0")
                .and(w -> w.eq(SysDept::getAncestors, String.valueOf(deptId))
                        .or().likeRight(SysDept::getAncestors, deptId + ",")
                        .or().likeLeft(SysDept::getAncestors, "," + deptId)
                        .or().like(SysDept::getAncestors, "," + deptId + ","))));
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default int hasChildByDeptId(Long deptId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, "0")
                .last("limit 1")));
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    default SysDept checkDeptNameUnique(String deptName, Long parentId) {
        return selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptName, deptName)
                .eq(SysDept::getParentId, parentId)
                .last("limit 1"));
    }

    /**
     * 新增部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    default int insertDept(SysDept dept) {
        return insert(dept);
    }

    /**
     * 修改部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    default int updateDept(SysDept dept) {
        return updateById(dept);
    }

    /**
     * 修改所在部门正常状态
     *
     * @param deptIds 部门ID组
     */
    default void updateDeptStatusNormal(Long[] deptIds) {
        update(null, Wrappers.<SysDept>lambdaUpdate()
                .set(SysDept::getStatus, "0")
                .in(SysDept::getDeptId, Arrays.asList(deptIds)));
    }

    /**
     * 修改子元素关系
     *
     * @param depts 子元素
     * @return 结果
     */
    int updateDeptChildren(@Param("depts") List<SysDept> depts);

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default int deleteDeptById(Long deptId) {
        return update(null, Wrappers.<SysDept>lambdaUpdate()
                .set(SysDept::getDelFlag, "2")
                .eq(SysDept::getDeptId, deptId));
    }
}
