package com.comonon.mall.user.service;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.constant.RedisKeys;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.user.config.SmsProps;
import com.comonon.mall.user.sms.SmsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务，包含频控与一次性校验。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TRIES_SUFFIX = ":tries";

    private final StringRedisTemplate redis;
    private final SmsProps props;
    private final SmsGateway smsGateway;

    /**
     * 发送验证码。带 cooldown / 24h / IP 频控。
     */
    public void send(String phone, String ip, String scene) {
        // cooldown 60s
        String cdKey = RedisKeys.smsCooldown(phone);
        Boolean cdSet = redis.opsForValue().setIfAbsent(cdKey, "1", props.getCooldown());
        if (cdSet == null || !cdSet) {
            throw new BizException(ErrorCode.SMS_SEND_TOO_FREQUENT, "发送过于频繁");
        }

        // daily 10
        String dailyKey = RedisKeys.smsDaily(phone);
        Long daily = redis.opsForValue().increment(dailyKey);
        if (daily != null && daily == 1L) {
            redis.expire(dailyKey, Duration.ofDays(1));
        }
        if (daily != null && daily > props.getDailyLimit()) {
            throw new BizException(ErrorCode.SMS_SEND_LIMIT_EXCEEDED, "短信发送上限");
        }

        // ip 5min 20
        if (ip != null && !ip.isEmpty()) {
            String ipKey = RedisKeys.smsIp(ip);
            Long ipCnt = redis.opsForValue().increment(ipKey);
            if (ipCnt != null && ipCnt == 1L) {
                redis.expire(ipKey, props.getIpWindow());
            }
            if (ipCnt != null && ipCnt > props.getIpLimit()) {
                throw new BizException(ErrorCode.SMS_SEND_LIMIT_EXCEEDED, "IP 发送上限");
            }
        }

        String code = generateCode();
        log.info("code :{}", code);
        String key = RedisKeys.smsCode(scene, phone);
        redis.opsForValue().set(key, code, props.getCodeTtl());
        redis.delete(key + TRIES_SUFFIX);
        smsGateway.send(phone, code);
    }

    /**
     * 校验验证码。错误累计达上限将作废。
     */
    public void verify(String phone, String code, String scene) {
        String key = RedisKeys.smsCode(scene, phone);
        String saved = redis.opsForValue().get(key);
        if (saved == null) {
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED, "验证码已过期");
        }
        if (!saved.equals(code)) {
            String triesKey = key + TRIES_SUFFIX;
            Long tries = redis.opsForValue().increment(triesKey);
            if (tries != null && tries == 1L) {
                redis.expire(triesKey, props.getCodeTtl());
            }
            if (tries != null && tries >= props.getMaxTries()) {
                redis.delete(key);
                redis.delete(triesKey);
            }
            throw new BizException(ErrorCode.SMS_CODE_INVALID, "验证码错误");
        }
        // 一次性
        redis.delete(key);
        redis.delete(key + TRIES_SUFFIX);
    }

    public void delete(String phone, String scene) {
        String key = RedisKeys.smsCode(scene, phone);
        redis.delete(key);
        redis.delete(key + TRIES_SUFFIX);
    }

    /** 6 位数字，100000–999999，避免前导 0 在日志里被误读成 5 位 */
    private String generateCode() {
        int n = 100_000 + RANDOM.nextInt(900_000);
        return String.valueOf(n);
    }
}
