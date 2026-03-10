package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charles.project.system.domain.SysRoleDept;
import org.apache.ibatis.executor.BatchResult;

import java.util.Arrays;
import java.util.List;

/**
 * 角色与部门关联表 数据层
 *
 * @author charles
 */
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /**
     * 通过角色ID删除角色和部门关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int deleteRoleDeptByRoleId(Long roleId) {
        return delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, roleId));
    }

    /**
     * 批量删除角色部门关联信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteRoleDept(Long[] ids) {
        return delete(new LambdaQueryWrapper<SysRoleDept>()
                .in(SysRoleDept::getRoleId, Arrays.asList(ids)));
    }

    /**
     * 查询部门使用数量
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default int selectCountRoleDeptByDeptId(Long deptId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getDeptId, deptId)));
    }

    /**
     * 批量新增角色部门信息
     *
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    default int batchRoleDept(List<SysRoleDept> roleDeptList) {
        List<BatchResult> results = insert(roleDeptList);
        return results.stream().map(t -> Arrays.stream(t.getUpdateCounts()).sum()).mapToInt(Integer::intValue).sum();
    }
}
