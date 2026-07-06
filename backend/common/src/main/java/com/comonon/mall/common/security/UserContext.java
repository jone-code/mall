package com.comonon.mall.common.security;

/**
 * 当前请求的用户上下文，由鉴权拦截器注入。
 */
public final class UserContext {

    private static final ThreadLocal<UserPrincipal> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(UserPrincipal p) {
        HOLDER.set(p);
    }

    public static UserPrincipal get() {
        return HOLDER.get();
    }

    public static Long userId() {
        UserPrincipal p = HOLDER.get();
        return p == null ? null : p.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
