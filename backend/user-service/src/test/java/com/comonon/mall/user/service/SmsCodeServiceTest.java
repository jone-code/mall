package com.comonon.mall.user.service;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.user.config.SmsProps;
import com.comonon.mall.user.sms.SmsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsCodeServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;
    @Mock SmsGateway smsGateway;

    SmsProps props;
    SmsCodeService service;

    @BeforeEach
    void setUp() {
        props = new SmsProps();
        props.setCodeTtl(Duration.ofMinutes(5));
        props.setCooldown(Duration.ofSeconds(60));
        props.setDailyLimit(10);
        props.setIpWindow(Duration.ofMinutes(5));
        props.setIpLimit(20);
        props.setMaxTries(5);
        when(redis.opsForValue()).thenReturn(valueOps);
        service = new SmsCodeService(redis, props, smsGateway);
    }

    @Test
    void send_ok() {
        when(valueOps.setIfAbsent(eq("sms:cooldown:13800000000"), eq("1"), any(Duration.class)))
                .thenReturn(true);
        when(valueOps.increment("sms:daily:13800000000")).thenReturn(1L);
        when(valueOps.increment("sms:ip:1.2.3.4")).thenReturn(1L);

        service.send("13800000000", "1.2.3.4", "LOGIN");

        verify(smsGateway).send(eq("13800000000"), anyString());
        verify(valueOps).set(eq("sms:LOGIN:13800000000"), anyString(), eq(props.getCodeTtl()));
    }

    @Test
    void send_cooldownHit() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> service.send("13800000000", "1.1.1.1", "LOGIN"));
        assertEquals(ErrorCode.SMS_SEND_TOO_FREQUENT, ex.getCode());
        verify(smsGateway, never()).send(any(), any());
    }

    @Test
    void send_dailyLimitHit() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(valueOps.increment("sms:daily:13800000000")).thenReturn(11L);

        BizException ex = assertThrows(BizException.class,
                () -> service.send("13800000000", "1.1.1.1", "LOGIN"));
        assertEquals(ErrorCode.SMS_SEND_LIMIT_EXCEEDED, ex.getCode());
    }

    @Test
    void send_ipLimitHit() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(valueOps.increment("sms:daily:13800000000")).thenReturn(2L);
        when(valueOps.increment("sms:ip:1.2.3.4")).thenReturn(21L);

        BizException ex = assertThrows(BizException.class,
                () -> service.send("13800000000", "1.2.3.4", "LOGIN"));
        assertEquals(ErrorCode.SMS_SEND_LIMIT_EXCEEDED, ex.getCode());
    }

    @Test
    void verify_codeExpired() {
        when(valueOps.get("sms:LOGIN:13800000000")).thenReturn(null);
        BizException ex = assertThrows(BizException.class,
                () -> service.verify("13800000000", "123456", "LOGIN"));
        assertEquals(ErrorCode.SMS_CODE_EXPIRED, ex.getCode());
    }

    @Test
    void verify_codeWrong_increasesTries() {
        when(valueOps.get("sms:LOGIN:13800000000")).thenReturn("000000");
        when(valueOps.increment("sms:LOGIN:13800000000:tries")).thenReturn(1L);
        BizException ex = assertThrows(BizException.class,
                () -> service.verify("13800000000", "123456", "LOGIN"));
        assertEquals(ErrorCode.SMS_CODE_INVALID, ex.getCode());
    }

    @Test
    void verify_okAndDelete() {
        when(valueOps.get("sms:LOGIN:13800000000")).thenReturn("123456");
        service.verify("13800000000", "123456", "LOGIN");
        verify(redis).delete("sms:LOGIN:13800000000");
        verify(redis).delete("sms:LOGIN:13800000000:tries");
    }

    @Test
    void verify_maxTries_invalidatesCode() {
        when(valueOps.get("sms:LOGIN:13800000000")).thenReturn("000000");
        when(valueOps.increment("sms:LOGIN:13800000000:tries")).thenReturn(5L);
        assertThrows(BizException.class,
                () -> service.verify("13800000000", "123456", "LOGIN"));
        verify(redis).delete("sms:LOGIN:13800000000");
    }
}
