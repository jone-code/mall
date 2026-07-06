package com.comonon.mall.user.oauth;

import org.springframework.stereotype.Component;

@Component
public class WechatOAuthStub implements WechatOAuthClient {
    @Override
    public WechatUser codeToOpenid(String code) {
        return new WechatUser("stub_openid_" + code, null);
    }
}
