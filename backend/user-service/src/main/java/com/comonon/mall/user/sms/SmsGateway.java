package com.comonon.mall.user.sms;

/**
 * 短信网关契约。生产环境替换为真实实现。
 */
public interface SmsGateway {
    /**
     * 发送验证码短信。
     */
    void send(String phone, String code);
}
