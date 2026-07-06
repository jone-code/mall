package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminUser;
import com.comonon.mall.admin.security.RedisTestSupport;
import com.comonon.mall.common.sms.SmsGateway;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    @Mock CaptchaService captchaService;
    @Mock AdminUserService adminUserService;
    @Mock PermissionService permissionService;
    @Mock AdminTokenService tokenService;
    @Mock AuditLogService auditLogService;
    @Mock SmsGateway smsGateway;

    StringRedisTemplate redis;
    BCryptPasswordEncoder encoder;

    AdminLoginService loginService;

    @BeforeEach
    void setUp() {
        redis = RedisTestSupport.fakeStringRedis();
        encoder = new BCryptPasswordEncoder(4);
        loginService = new AdminLoginService(
                captchaService, adminUserService, permissionService,
                tokenService, auditLogService, encoder, redis, smsGateway);
        ReflectionTestUtils.setField(loginService, "maxFail", 5);
        ReflectionTestUtils.setField(loginService, "failWindowSeconds", 600L);
        ReflectionTestUtils.setField(loginService, "lockSeconds", 1800L);
        ReflectionTestUtils.setField(loginService, "challengeTtlSeconds", 300L);
        ReflectionTestUtils.setField(loginService, "smsLength", 6);
    }

    private AdminUser sampleUser(String pwd) {
        AdminUser u = new AdminUser();
        u.setId(7L);
        u.setUsername("alice");
        u.setPassword(encoder.encode(pwd));
        u.setPhone("13800138000");
        u.setStatus(0);
        return u;
    }

    @Test
    void passwordStep_happyPath_issuesChallenge() {
        AdminUser u = sampleUser("CorrectHorse!23");
        when(adminUserService.findByUsername("alice")).thenReturn(u);

        var result = loginService.passwordStep("alice", "CorrectHorse!23",
                "cap-id", "abcd", "127.0.0.1", "ua");

        assertNotNull(result.getChallengeToken());
        assertTrue(result.getPhoneMasked().contains("****"));
        verify(captchaService).verifyAndConsume("cap-id", "abcd");
        verify(smsGateway).sendCode(eq("13800138000"), any());
    }

    @Test
    void verifyStep_happyPath_issuesTokens_andRevokesOtherSessions() {
        // 准备：先走 password 步骤拿到 challengeToken
        AdminUser u = sampleUser("CorrectHorse!23");
        when(adminUserService.findByUsername("alice")).thenReturn(u);
        var step1 = loginService.passwordStep("alice", "CorrectHorse!23",
                "cap-id", "abcd", "127.0.0.1", "ua");

        // 取 redis 内的 smsCode（fake redis）
        String smsCode = (String) redis.opsForHash()
                .get("admin:challenge:" + step1.getChallengeToken(), "smsCode");
        assertNotNull(smsCode);

        when(permissionService.loadPermissionCodes(7L)).thenReturn(List.of("admin:user:read"));
        when(permissionService.loadRoleCodes(7L)).thenReturn(List.of("ops"));
        when(permissionService.computePermsHash(any())).thenReturn("hash");
        when(tokenService.issue(eq(7L), eq("alice"), any(), eq("hash")))
                .thenReturn(new AdminTokenService.IssuedTokens("ACC", "REF",
                        "sid-1", "jti-1", 1800, 86400));

        var v = loginService.verifyStep(step1.getChallengeToken(), smsCode, "127.0.0.1", "ua");

        assertEquals("ACC", v.getAccessToken());
        assertEquals("REF", v.getRefreshToken());
        verify(tokenService).revokeAllSessions(7L);  // 同账号互踢
        // challenge 一次性
        assertFalse(redis.hasKey("admin:challenge:" + step1.getChallengeToken()));
    }

    @Test
    void passwordStep_lockoutAfterMaxFail() {
        AdminUser u = sampleUser("CorrectHorse!23");
        when(adminUserService.findByUsername("alice")).thenReturn(u);

        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () -> loginService.passwordStep(
                    "alice", "WrongPwd!23A", "cap", "ok", "ip", "ua"));
        }
        // 第6次：因锁定直接 LOCKED
        BusinessException ex = assertThrows(BusinessException.class, () -> loginService.passwordStep(
                "alice", "CorrectHorse!23", "cap", "ok", "ip", "ua"));
        assertEquals(ErrorCodes.ACCOUNT_LOCKED, ex.getCode());
    }

    @Test
    void verifyStep_invalidSmsThrows() {
        AdminUser u = sampleUser("CorrectHorse!23");
        when(adminUserService.findByUsername("alice")).thenReturn(u);
        var step1 = loginService.passwordStep("alice", "CorrectHorse!23",
                "cap-id", "abcd", "127.0.0.1", "ua");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                loginService.verifyStep(step1.getChallengeToken(), "000000-wrong", "ip", "ua"));
        assertEquals(ErrorCodes.INVALID_SMS, ex.getCode());
    }
}
