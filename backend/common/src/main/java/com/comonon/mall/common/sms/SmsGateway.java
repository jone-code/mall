package com.comonon.mall.common.sms;

/**
 * 短信网关存根。生产环境替换为真实实现。
 */
public interface SmsGateway {

    /**
     * 发送验证码短信。
     * @return true 表示已成功投递到下游网关
     */
    boolean sendCode(String phone, String code);
}
