package com.comonon.mall.common.constant;

/**
 * user-service 专用 Redis Key（短信限流等）。
 * 与 {@link com.comonon.mall.common.security.RedisKeys} 重叠的会话/JWT 键委托至 security 包，避免双份漂移。
 */
public final class RedisKeys {
    private RedisKeys() {}

    public static String smsCode(String scene, String phone) {
        return com.comonon.mall.common.security.RedisKeys.smsCode(scene, phone);
    }

    public static String smsCooldown(String phone) {
        return com.comonon.mall.common.security.RedisKeys.smsCooldown(phone);
    }

    /** 同手机号 24h 上限 10 次 */
    public static String smsDaily(String phone) {
        return "sms:daily:" + phone;
    }

    /** 同 IP 5 分钟 20 次 */
    public static String smsIp(String ip) {
        return "sms:ip:" + ip;
    }

    public static String session(String sid) {
        return com.comonon.mall.common.security.RedisKeys.session(sid);
    }

    public static String userSessions(long userId) {
        return com.comonon.mall.common.security.RedisKeys.userSessions(userId);
    }

    public static String userDevice(long userId, String deviceType) {
        return com.comonon.mall.common.security.RedisKeys.userDevSession(userId, deviceType);
    }

    public static String jwtBlacklist(String jti) {
        return com.comonon.mall.common.security.RedisKeys.jwtBlacklist(jti);
    }

    /** SMS 场景 */
    public static final String SCENE_LOGIN = "LOGIN";
}
