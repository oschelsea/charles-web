package com.xenon.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnumValueValidator 单元测试
 */
class EnumValueValidatorTest {

    private EnumValueValidator validator;
    private ConstraintValidatorContext context;

    enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    @BeforeEach
    void setUp() {
        validator = new EnumValueValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    private void initValidator(boolean allowNull) {
        // 创建匿名类实现 EnumValue 接口
        EnumValue annotation = new EnumValue() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return TestEnum.class;
            }
            @Override
            public boolean allowNull() {
                return allowNull;
            }
            @Override
            public String message() {
                return "值必须在枚举范围内";
            }
            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return EnumValue.class;
            }
        };
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("有效枚举值 - 字符串")
    void testValidEnumValueString() {
        initValidator(true);
        assertTrue(validator.isValid("VALUE1", context));
        assertTrue(validator.isValid("VALUE2", context));
        assertTrue(validator.isValid("VALUE3", context));
    }

    @Test
    @DisplayName("有效枚举值 - 枚举类型")
    void testValidEnumValueEnum() {
        initValidator(true);
        assertTrue(validator.isValid(TestEnum.VALUE1, context));
        assertTrue(validator.isValid(TestEnum.VALUE2, context));
    }

    @Test
    @DisplayName("无效枚举值 - 字符串")
    void testInvalidEnumValue() {
        initValidator(true);
        assertFalse(validator.isValid("INVALID", context));
        assertFalse(validator.isValid("value1", context)); // 大小写敏感
    }

    @Test
    @DisplayName("null值 - allowNull=true")
    void testNullValueAllowNull() {
        initValidator(true);
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("null值 - allowNull=false")
    void testNullValueDisallowNull() {
        initValidator(false);
        assertFalse(validator.isValid(null, context));
    }

    @Test
    @DisplayName("空字符串 - 校验失败")
    void testEmptyString() {
        initValidator(true);
        assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("数字值 - 校验失败")
    void testNumericValue() {
        initValidator(true);
        assertFalse(validator.isValid(123, context));
    }
}
