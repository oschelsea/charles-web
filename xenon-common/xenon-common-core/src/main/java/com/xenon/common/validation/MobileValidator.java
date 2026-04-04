package com.xenon.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 中国大陆手机号校验器
 * 校验规则：以1开头的11位数字
 *
 * @author charles
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {

    /**
     * 中国大陆手机号正则：以1开头的11位数字
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public void initialize(Mobile constraintAnnotation) {
        // 无需初始化
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 值通过校验，如需必填请配合 @NotBlank 使用
        if (value == null) {
            return true;
        }

        return MOBILE_PATTERN.matcher(value).matches();
    }
}
