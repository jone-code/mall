package com.comonon.mall.admin.service;

import com.comonon.mall.common.security.RedisKeys;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import com.wf.captcha.SpecCaptcha;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/**
 * 图形验证码：4 位字符，captchaId UUID，Redis 存 captcha:{id} TTL 5min。
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redis;

    @Value("${mall.admin.captcha.ttl-seconds:300}")
    private long ttlSeconds;

    @Value("${mall.admin.captcha.length:4}")
    private int length;

    public CaptchaResult generate() {
        SpecCaptcha captcha = new SpecCaptcha(120, 40, length);
        String text = captcha.text().toLowerCase(Locale.ROOT);
        String base64 = captcha.toBase64();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(RedisKeys.captcha(captchaId), text, Duration.ofSeconds(ttlSeconds));
        return new CaptchaResult(captchaId, base64);
    }

    /**
     * 校验并删除（一次性）。失败抛 INVALID_CAPTCHA。
     */
    public void verifyAndConsume(String captchaId, String input) {
        if (captchaId == null || input == null || captchaId.isBlank() || input.isBlank()) {
            throw new BusinessException(ErrorCodes.INVALID_CAPTCHA, "图形验证码不能为空");
        }
        String key = RedisKeys.captcha(captchaId);
        String stored = redis.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException(ErrorCodes.INVALID_CAPTCHA, "图形验证码已过期");
        }
        // 一次性，无论成功失败都删
        redis.delete(key);
        if (!stored.equalsIgnoreCase(input.trim())) {
            throw new BusinessException(ErrorCodes.INVALID_CAPTCHA, "图形验证码错误");
        }
    }

    @Data
    public static class CaptchaResult {
        private final String captchaId;
        /** data:image/png;base64,xxx */
        private final String image;
    }
}
