package com.xenon.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdCardValidator 单元测试
 */
class IdCardValidatorTest {

    private IdCardValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new IdCardValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("有效18位身份证号")
    void testValid18BitIdCard() {
        // 11010519491231002X - 有效的18位身份证号
        assertTrue(validator.isValid("11010519491231002X", context));
        assertTrue(validator.isValid("11010519491231002x", context)); // 小写x也有效
    }

    @Test
    @DisplayName("有效15位身份证号")
    void testValid15BitIdCard() {
        // 110105491231001 - 有效的15位身份证号
        assertTrue(validator.isValid("110105491231001", context));
    }

    @Test
    @DisplayName("null值 - 通过校验")
    void testNullIdCard() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("空字符串 - 校验失败")
    void testEmptyIdCard() {
        assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("长度不正确 - 校验失败")
    void testInvalidLength() {
        assertFalse(validator.isValid("1101051949123100", context)); // 17位
        assertFalse(validator.isValid("1101051949123100234", context)); // 19位
    }

    @Test
    @DisplayName("无效校验位 - 校验失败")
    void testInvalidCheckCode() {
        // 修改最后一位，使校验失败
        assertFalse(validator.isValid("110105194912310021", context));
    }

    @Test
    @DisplayName("无效日期 - 校验失败")
    void testInvalidDate() {
        // 月份为13
        assertFalse(validator.isValid("11010519491331002X", context));
        // 日期为32
        assertFalse(validator.isValid("11010519491232002X", context));
    }

    @Test
    @DisplayName("包含非数字字符 - 校验失败")
    void testNonDigit() {
        assertFalse(validator.isValid("11010519491231002A", context)); // 最后一位不是X
        assertFalse(validator.isValid("11010519491231002!", context));
    }
}
