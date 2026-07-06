package com.comonon.mall.common.web;

/**
 * 错误码集中定义（auth-design §9）。
 */
public final class ErrorCodes {

    private ErrorCodes() {}

    public static final int INVALID_CODE          = 40101;
    public static final int CODE_EXPIRED          = 40102;
    public static final int SEND_TOO_FREQUENT     = 40103;
    public static final int SMS_DAILY_LIMIT       = 40104;

    public static final int REFRESH_INVALID       = 40110;
    public static final int ACCESS_INVALID        = 40111;

    public static final int ACCOUNT_DISABLED      = 40301;
    public static final int ACCOUNT_DEREGISTERED  = 40302;

    /* admin */
    public static final int INVALID_CAPTCHA       = 40120;
    public static final int INVALID_CREDENTIALS   = 40121;
    public static final int ACCOUNT_LOCKED        = 42301;
    public static final int INVALID_SMS           = 40122;
    public static final int CHALLENGE_EXPIRED     = 40123;

    public static final int PERMISSION_DENIED     = 40330;
    public static final int PERMS_VERSION_STALE   = 40331;

    public static final int PASSWORD_POLICY       = 40124;
    public static final int PASSWORD_REUSED       = 40125;
}
