package io.charles.project.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.DateUtils;
import io.charles.common.utils.StringUtils;
import io.charles.project.monitor.domain.SysLogininfor;

import java.util.Arrays;
import java.util.List;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author charles
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {
    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    default void insertLogininfor(SysLogininfor logininfor) {
        if (logininfor.getLoginTime() == null) {
            logininfor.setLoginTime(DateUtils.getTime());
        }
        insert(logininfor);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    default List<SysLogininfor> selectLogininforList(SysLogininfor logininfor) {
        LambdaQueryWrapper<SysLogininfor> wrapper = Wrappers.lambdaQuery();
        if (logininfor != null) {
            wrapper.like(StringUtils.isNotEmpty(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
                    .eq(StringUtils.isNotEmpty(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
                    .like(StringUtils.isNotEmpty(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName());

            if (logininfor.getParams() != null) {
                if (logininfor.getParams().get("beginTime") != null && logininfor.getParams().get("endTime") != null) {
                    wrapper.between(SysLogininfor::getLoginTime, logininfor.getParams().get("beginTime"), logininfor.getParams().get("endTime"));
                } else if (logininfor.getParams().get("beginTime") != null) {
                    wrapper.ge(SysLogininfor::getLoginTime, logininfor.getParams().get("beginTime"));
                } else if (logininfor.getParams().get("endTime") != null) {
                    wrapper.le(SysLogininfor::getLoginTime, logininfor.getParams().get("endTime"));
                }
            }
        }
        wrapper.orderByDesc(SysLogininfor::getInfoId);
        return selectList(wrapper);
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    default int deleteLogininforByIds(Long[] infoIds) {
        return deleteByIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     *
     * @return 结果
     */
    public int cleanLogininfor();
}
