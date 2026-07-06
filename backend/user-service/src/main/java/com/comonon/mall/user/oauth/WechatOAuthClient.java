package com.comonon.mall.user.oauth;

public interface WechatOAuthClient {
    /**
     * 用 code 换取 openid（与 unionid）。
     */
    WechatUser codeToOpenid(String code);

    record WechatUser(String openid, String unionid) {}
}
