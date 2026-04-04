package com.xenon.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 枚举值校验注解
 * 校验值是否在指定枚举范围内
 *
 * @author charles
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
public @interface EnumValue {

    /**
     * 错误消息
     */
    String message() default "值必须在枚举范围内";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 是否允许空值
     */
    boolean allowNull() default true;
}
