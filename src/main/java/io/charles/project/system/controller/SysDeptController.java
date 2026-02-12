package io.charles.project.system.controller;

import io.charles.common.constant.UserConstants;
import io.charles.common.utils.StringUtils;
import io.charles.framework.aspectj.lang.annotation.Log;
import io.charles.framework.aspectj.lang.enums.BusinessType;
import io.charles.framework.web.controller.BaseController;
import io.charles.framework.web.domain.R;
import io.charles.framework.web.domain.TreeSelect;
import io.charles.project.system.domain.SysDept;
import io.charles.project.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门信息
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {
    private final ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public R<List<SysDept>> excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId
                || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        deptService.checkDeptDataScope(deptId);
        return R.ok(deptService.selectDeptById(deptId));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public R<List<TreeSelect>> treeselect(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return R.ok(deptService.buildDeptTreeSelect(depts));
    }

    public record DeptTreeVo(List<Integer> checkedKeys, List<TreeSelect> depts) {
    }

    /**
     * 加载对应角色部门列表树
     */
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public R<DeptTreeVo> roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        return R.ok(new DeptTreeVo(deptService.selectDeptListByRoleId(roleId), deptService.buildDeptTreeSelect(depts)));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDept dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return R.fail("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getUsername());
        return toResult(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDept dept) {
        if (UserConstants.NOT_UNIQUE.equals(deptService.checkDeptNameUnique(dept))) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(dept.getDeptId())) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())
                && deptService.selectNormalChildrenDeptById(dept.getDeptId()) > 0) {
            return R.fail("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getUsername());
        return toResult(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return R.fail("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return R.fail("部门存在用户,不允许删除");
        }
        return toResult(deptService.deleteDeptById(deptId));
    }
}
