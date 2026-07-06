package com.comonon.mall.user.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsGatewayStub implements SmsGateway {
    @Override
    public void send(String phone, String code) {
        log.info("【STUB SMS】phone={} | 验证码=[{}] | 共{}位（请原样输入）", phone, code, code.length());
    }
}
