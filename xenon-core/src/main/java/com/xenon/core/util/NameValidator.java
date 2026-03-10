package com.xenon.core.util;

import java.util.regex.Pattern;

/**
 * 资源名称校验工具类（工作空间、图层、数据存储）。
 * 规则：
 * - 允许字母、数字、中文、下划线(_)和连字符(-)
 * - 不能以数字或连字符开头
 * - 至少1个字符
 */
public final class NameValidator {

    /**
     * 有效名称正则表达式：
     * - 必须以字母、中文或下划线开头
     * - 可以包含字母、数字、中文、下划线和连字符
     */
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z_\\u4e00-\\u9fa5][a-zA-Z0-9_\\-\\u4e00-\\u9fa5]*$");

    private NameValidator() {
        // 工具类
    }

    /**
     * 验证资源名称是否有效。
     *
     * @param name 待验证的名称
     * @return 如果名称有效返回true，否则返回false
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return VALID_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 验证资源名称，如果无效则抛出异常。
     *
     * @param name 待验证的名称
     * @param resourceType 资源类型（用于错误提示）
     * @throws IllegalArgumentException 如果名称无效
     */
    public static void validateName(String name, String resourceType) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException(
                String.format("无效的%s名称 '%s'。名称必须以字母、中文或下划线开头，只能包含字母、数字、中文、下划线(_)和连字符(-)。", 
                    resourceType, name)
            );
        }
    }
}
