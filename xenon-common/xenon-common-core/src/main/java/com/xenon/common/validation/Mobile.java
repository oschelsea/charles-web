package com.xenon.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 中国大陆手机号校验注解
 * 校验规则：以1开头的11位数字
 *
 * @author charles
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = MobileValidator.class)
public @interface Mobile {

    /**
     * 错误消息
     */
    String message() default "手机号格式不正确";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
