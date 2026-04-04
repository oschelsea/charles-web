package com.xenon.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 中国大陆身份证号校验器
 * 支持15位和18位身份证号，含校验位验证
 *
 * @author charles
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    /**
     * 15位身份证号正则
     */
    private static final Pattern ID_CARD_15_PATTERN = Pattern.compile("^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$");

    /**
     * 18位身份证号正则
     */
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");

    /**
     * 18位身份证校验码权重
     */
    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 18位身份证校验码对照表
     */
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    @Override
    public void initialize(IdCard constraintAnnotation) {
        // 无需初始化
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 值通过校验，如需必填请配合 @NotBlank 使用
        if (value == null) {
            return true;
        }

        // 去除空格
        String idCard = value.trim();

        // 根据长度选择校验规则
        if (idCard.length() == 15) {
            return validate15BitIdCard(idCard);
        } else if (idCard.length() == 18) {
            return validate18BitIdCard(idCard);
        }

        return false;
    }

    /**
     * 校验15位身份证号
     */
    private boolean validate15BitIdCard(String idCard) {
        return ID_CARD_15_PATTERN.matcher(idCard).matches();
    }

    /**
     * 校验18位身份证号（含校验位验证）
     */
    private boolean validate18BitIdCard(String idCard) {
        // 基本格式校验
        if (!ID_CARD_18_PATTERN.matcher(idCard).matches()) {
            return false;
        }

        // 校验位验证
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHT[i];
        }

        char expectedCheckCode = CHECK_CODE[sum % 11];
        char actualCheckCode = Character.toUpperCase(idCard.charAt(17));

        return expectedCheckCode == actualCheckCode;
    }
}
