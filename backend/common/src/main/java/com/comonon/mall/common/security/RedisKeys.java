package com.comonon.mall.common.security;

/**
 * Redis Key 集中定义。
 */
public final class RedisKeys {

    private RedisKeys() {}

    /* === C 端 === */
    public static String smsCode(String scene, String phone) { return "sms:" + scene + ":" + phone; }
    public static String smsCooldown(String phone)           { return "sms:cooldown:" + phone; }
    public static String session(String sid)                 { return "session:" + sid; }
    public static String userSessions(long userId)           { return "user:sessions:" + userId; }
    public static String userDevSession(long userId, String dt) { return "user:dev:" + userId + ":" + dt; }
    public static String jwtBlacklist(String jti)            { return "jwt:bl:" + jti; }

    /* === 管理后台 === */
    public static String captcha(String captchaId)           { return "captcha:" + captchaId; }
    public static String adminLoginFail(String username)     { return "admin:login:fail:" + username; }
    public static String adminLoginLock(String username)     { return "admin:login:lock:" + username; }
    public static String adminChallenge(String token)        { return "admin:challenge:" + token; }
    public static String adminUserSessions(long adminUserId) { return "admin:user:sessions:" + adminUserId; }
    public static String adminPermsVersion(long adminUserId) { return "admin:perms:version:" + adminUserId; }
}
