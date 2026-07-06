package com.comonon.mall.common.sms;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认 stub：仅打日志（脱敏），不发真实短信。开发与单测可用。
 */
@Slf4j
public class SmsGatewayStub implements SmsGateway {
    @Override
    public boolean sendCode(String phone, String code) {
        log.info("[SmsGatewayStub] phone={} code={}", maskPhone(phone), code);
        return true;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
