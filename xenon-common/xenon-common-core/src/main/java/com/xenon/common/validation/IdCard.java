package com.xenon.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 中国大陆身份证号校验注解
 * 支持15位和18位身份证号
 *
 * @author charles
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = IdCardValidator.class)
public @interface IdCard {

    /**
     * 错误消息
     */
    String message() default "身份证号格式不正确";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
