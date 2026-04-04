package com.xenon.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MobileValidator 单元测试
 */
class MobileValidatorTest {

    private MobileValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new MobileValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("有效手机号 - 13812345678")
    void testValidMobile() {
        assertTrue(validator.isValid("13812345678", context));
    }

    @Test
    @DisplayName("有效手机号 - 15912345678")
    void testValidMobile159() {
        assertTrue(validator.isValid("15912345678", context));
    }

    @Test
    @DisplayName("有效手机号 - 18812345678")
    void testValidMobile188() {
        assertTrue(validator.isValid("18812345678", context));
    }

    @Test
    @DisplayName("null值 - 通过校验")
    void testNullMobile() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("空字符串 - 校验失败")
    void testEmptyMobile() {
        assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("非11位 - 校验失败")
    void testInvalidLength() {
        assertFalse(validator.isValid("1381234567", context));
        assertFalse(validator.isValid("138123456789", context));
    }

    @Test
    @DisplayName("不以1开头 - 校验失败")
    void testNotStartWith1() {
        assertFalse(validator.isValid("23812345678", context));
    }

    @Test
    @DisplayName("包含非数字字符 - 校验失败")
    void testNonDigit() {
        assertFalse(validator.isValid("1381234567a", context));
    }

    @Test
    @DisplayName("第二位不在3-9范围 - 校验失败")
    void testInvalidSecondDigit() {
        assertFalse(validator.isValid("10812345678", context));
        assertFalse(validator.isValid("12812345678", context));
    }
}
