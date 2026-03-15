package com.xenon.common.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * @author Chelsea
 */
@Component
public class SpringUtils extends SpringUtil {
    /**
     * 获取aop代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        if (AopUtils.isAopProxy(invoker)) {
            return (T) AopContext.currentProxy();
        }
        return (T) getBean(invoker.getClass());
    }
}
