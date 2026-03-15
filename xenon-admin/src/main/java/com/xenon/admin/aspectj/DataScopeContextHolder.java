package com.xenon.admin.aspectj;

/**
 * 数据权限当前线程变量
 *
 * @author charles
 */
public class DataScopeContextHolder {
    private static final ThreadLocal<String> DATA_SCOPE_THREAD_LOCAL = new ThreadLocal<>();

    public static void set(String dataScope) {
        DATA_SCOPE_THREAD_LOCAL.set(dataScope);
    }

    public static String get() {
        return DATA_SCOPE_THREAD_LOCAL.get();
    }

    public static void clear() {
        DATA_SCOPE_THREAD_LOCAL.remove();
    }
}
