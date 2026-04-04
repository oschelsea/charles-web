package com.xenon.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 枚举值校验器
 * 校验值是否在指定枚举范围内
 *
 * @author charles
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

    private Set<String> enumValues;
    private boolean allowNull;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        // 获取枚举类的所有值
        this.enumValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // null 值处理
        if (value == null) {
            return allowNull;
        }

        // 将值转换为字符串并检查是否在枚举范围内
        String strValue = value.toString();

        // 如果是枚举类型，直接比较 name
        if (value instanceof Enum) {
            return enumValues.contains(((Enum<?>) value).name());
        }

        // 否则按字符串比较
        return enumValues.contains(strValue);
    }
}
