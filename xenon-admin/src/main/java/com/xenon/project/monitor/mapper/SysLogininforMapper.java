package com.xenon.project.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xenon.common.utils.DateUtils;
import com.xenon.common.utils.StringUtils;
import com.xenon.common.utils.WrapperBuilder;
import com.xenon.system.domain.SysLogininfor;

import java.time.LocalDateTime;
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
        return selectLogininforList(null, logininfor);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    default List<SysLogininfor> selectLogininforList(IPage<SysLogininfor> page, SysLogininfor logininfor) {
        LambdaQueryWrapper<SysLogininfor> wrapper = Wrappers.lambdaQuery();
        if (logininfor != null) {
            wrapper.like(StringUtils.isNotEmpty(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
                    .eq(StringUtils.isNotEmpty(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
                    .like(StringUtils.isNotEmpty(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName());

            if (logininfor.getParams() != null) {
                LocalDateTime beginTime = WrapperBuilder.parseDateTime(logininfor.getParams().get("beginTime"));
                LocalDateTime endTime = WrapperBuilder.parseDateTime(logininfor.getParams().get("endTime"));
                wrapper.ge(beginTime != null, SysLogininfor::getLoginTime, beginTime)
                       .le(endTime != null, SysLogininfor::getLoginTime, endTime);
            }
        }
        wrapper.orderByDesc(SysLogininfor::getInfoId);
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
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
