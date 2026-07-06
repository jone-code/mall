package com.comonon.mall.admin.security;

/**
 * 当前请求线程上下文：拦截器注入，控制器/AOP/审计读取。
 */
public final class AdminContext {

    private AdminContext() {}

    private static final ThreadLocal<Holder> HOLDER = new ThreadLocal<>();

    public static void set(Long adminUserId, String username, java.util.List<String> roles,
                           java.util.Set<String> permissions, String sid, String jti) {
        HOLDER.set(new Holder(adminUserId, username, roles, permissions, sid, jti));
    }

    public static Holder get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record Holder(Long adminUserId, String username,
                         java.util.List<String> roles,
                         java.util.Set<String> permissions,
                         String sid, String jti) {}
}
