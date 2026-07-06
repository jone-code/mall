package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.entity.AdminUser;
import com.comonon.mall.common.security.RedisKeys;
import com.comonon.mall.common.sms.SmsGateway;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理后台两步登录：
 *   step1 password（图形验证码 + 账号密码） -> challengeToken + 短信码
 *   step2 verify   （challengeToken + 短信码） -> access/refresh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLoginService {

    private final CaptchaService captchaService;
    private final AdminUserService adminUserService;
    private final PermissionService permissionService;
    private final AdminTokenService tokenService;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;
    private final SmsGateway smsGateway;

    @Value("${mall.admin.login.max-fail:5}")
    private int maxFail;

    @Value("${mall.admin.login.fail-window-seconds:600}")
    private long failWindowSeconds;

    @Value("${mall.admin.login.lock-seconds:1800}")
    private long lockSeconds;

    @Value("${mall.admin.challenge.ttl-seconds:300}")
    private long challengeTtlSeconds;

    @Value("${mall.admin.challenge.sms-length:6}")
    private int smsLength;

    private static final SecureRandom RNG = new SecureRandom();

    public PasswordStepResult passwordStep(String username, String password,
                                           String captchaId, String captcha,
                                           String ip, String userAgent) {
        captchaService.verifyAndConsume(captchaId, captcha);

        // 锁定检查
        String lockKey = RedisKeys.adminLoginLock(username);
        if (Boolean.TRUE.equals(redis.hasKey(lockKey))) {
            throw new BusinessException(ErrorCodes.ACCOUNT_LOCKED, "账号已锁定，请稍后再试");
        }

        AdminUser user = adminUserService.findByUsername(username);
        boolean ok = user != null
                && (user.getStatus() == null || user.getStatus() == 0)
                && passwordEncoder.matches(password, user.getPassword());

        if (!ok) {
            recordFail(username, user, ip, userAgent);
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "用户名或密码错误");
        }
        // 成功后清失败计数
        redis.delete(RedisKeys.adminLoginFail(username));

        // 生成 challengeToken
        String token = UUID.randomUUID().toString().replace("-", "");
        String smsCode = randomDigits(smsLength);
        String key = RedisKeys.adminChallenge(token);
        Map<String, String> hash = new HashMap<>();
        hash.put("username", username);
        hash.put("adminUserId", String.valueOf(user.getId()));
        hash.put("phone", user.getPhone());
        hash.put("smsCode", smsCode);
        hash.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redis.opsForHash().putAll(key, hash);
        redis.expire(key, Duration.ofSeconds(challengeTtlSeconds));

        smsGateway.sendCode(user.getPhone(), smsCode);

        return new PasswordStepResult(token, mask(user.getPhone()), challengeTtlSeconds);
    }

    private void recordFail(String username, AdminUser user, String ip, String ua) {
        String key = RedisKeys.adminLoginFail(username);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(failWindowSeconds));
        }
        if (count != null && count >= maxFail) {
            redis.opsForValue().set(RedisKeys.adminLoginLock(username), "1",
                    Duration.ofSeconds(lockSeconds));
        }
        AdminAuditLog log = new AdminAuditLog();
        if (user != null) {
            log.setAdminUserId(user.getId());
        }
        log.setUsername(username);
        log.setAction("LOGIN_FAIL");
        log.setRequestIp(ip);
        log.setUserAgent(ua);
        log.setResult(0);
        log.setErrorMsg("invalid credentials");
        log.setCreatedAt(LocalDateTime.now());
        auditLogService.log(log);
    }

    public VerifyStepResult verifyStep(String challengeToken, String smsCode, String ip, String userAgent) {
        if (challengeToken == null || smsCode == null) {
            throw new BusinessException(ErrorCodes.INVALID_SMS, "参数缺失");
        }
        String key = RedisKeys.adminChallenge(challengeToken);
        Map<Object, Object> hash = redis.opsForHash().entries(key);
        if (hash == null || hash.isEmpty()) {
            throw new BusinessException(ErrorCodes.CHALLENGE_EXPIRED, "challengeToken 无效或已过期");
        }
        Object expected = hash.get("smsCode");
        if (expected == null || !expected.toString().equals(smsCode.trim())) {
            throw new BusinessException(ErrorCodes.INVALID_SMS, "短信验证码错误");
        }
        Long adminUserId = Long.valueOf(hash.get("adminUserId").toString());
        String username = hash.get("username").toString();
        // 一次性
        redis.delete(key);

        // 互踢同账号其他会话
        tokenService.revokeAllSessions(adminUserId);

        // 计算权限指纹
        List<String> codes = permissionService.loadPermissionCodes(adminUserId);
        List<String> roles = permissionService.loadRoleCodes(adminUserId);
        String permsHash = permissionService.computePermsHash(codes);

        AdminTokenService.IssuedTokens tokens = tokenService.issue(adminUserId, username, roles, permsHash);

        adminUserService.touchLogin(adminUserId, ip);

        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(adminUserId);
        log.setUsername(username);
        log.setAction("LOGIN");
        log.setRequestIp(ip);
        log.setUserAgent(userAgent);
        log.setResult(1);
        log.setCreatedAt(LocalDateTime.now());
        auditLogService.log(log);

        return new VerifyStepResult(tokens.getAccessToken(), tokens.getRefreshToken(),
                tokens.getAccessTtlSec(), tokens.getRefreshTtlSec(),
                adminUserId, username, roles, codes);
    }

    private static String randomDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    private static String mask(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Data
    public static class PasswordStepResult {
        private final String challengeToken;
        private final String phoneMasked;
        private final long expiresIn;
    }

    @Data
    public static class VerifyStepResult {
        private final String accessToken;
        private final String refreshToken;
        private final long accessExpiresIn;
        private final long refreshExpiresIn;
        private final Long adminUserId;
        private final String username;
        private final List<String> roles;
        private final List<String> permissions;
    }
}
