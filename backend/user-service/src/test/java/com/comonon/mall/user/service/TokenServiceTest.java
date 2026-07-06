package com.comonon.mall.user.service;

import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.user.config.TokenProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;
    @Mock SetOperations<String, String> setOps;
    @Mock HashOperations<String, Object, Object> hashOps;

    JwtUtil jwtUtil;
    TokenProps props;
    TokenService tokenService;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef0123456789abcdef", Duration.ofHours(2));
        props = new TokenProps();
        props.setRefreshTtl(Duration.ofDays(7));
        props.setAbsoluteMax(Duration.ofDays(30));
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
        lenient().when(redis.opsForSet()).thenReturn(setOps);
        lenient().when(redis.opsForHash()).thenReturn(hashOps);
        tokenService = new TokenService(jwtUtil, redis, props);
    }

    @Test
    void issue_signsAccessAndRefresh() {
        when(valueOps.get("user:dev:1:APP_IOS")).thenReturn(null);
        TokenService.TokenPair pair = tokenService.issue(1L, "dev1", "APP_IOS");
        assertNotNull(pair.getAccessToken());
        assertNotNull(pair.getRefreshToken());
        assertTrue(pair.getSid().startsWith("sess_"));
        verify(hashOps).putAll(eq("session:" + pair.getSid()), anyMap());
        verify(redis).expire(eq("session:" + pair.getSid()), eq(props.getRefreshTtl()));
        verify(setOps).add("user:sessions:1", pair.getSid());
        verify(valueOps).set(eq("user:dev:1:APP_IOS"), eq(pair.getSid()), eq(props.getRefreshTtl()));
    }

    @Test
    void issue_kicksSameDeviceTypeOldSession() {
        // 旧会话 sid=oldsid，现有 dev key 指向 oldsid
        when(valueOps.get("user:dev:7:APP_IOS")).thenReturn("oldsid");
        Map<Object, Object> oldSession = new HashMap<>();
        oldSession.put(TokenService.F_USER_ID, "7");
        oldSession.put(TokenService.F_DEVICE_TYPE, "APP_IOS");
        oldSession.put(TokenService.F_ACCESS_JTI, "oldjti");
        oldSession.put(TokenService.F_ACCESS_EXP, String.valueOf(System.currentTimeMillis()/1000 + 1000));
        when(hashOps.entries("session:oldsid")).thenReturn(oldSession);
        when(valueOps.get("user:dev:7:APP_IOS"))
                .thenReturn("oldsid")  // for kick
                .thenReturn("oldsid"); // unused

        tokenService.issue(7L, "devX", "APP_IOS");

        verify(setOps).remove("user:sessions:7", "oldsid");
        verify(redis).delete("session:oldsid");
        verify(valueOps).set(eq("jwt:bl:oldjti"), eq("1"), any(Duration.class));
    }

    @Test
    void isJtiBlacklisted_delegatesHasKey() {
        when(redis.hasKey("jwt:bl:abc")).thenReturn(true);
        assertTrue(tokenService.isJtiBlacklisted("abc"));
    }

    @Test
    void logoutAll_kicksAllSessions() {
        when(setOps.members("user:sessions:9")).thenReturn(Set.of("s1", "s2"));
        when(hashOps.entries(anyString())).thenReturn(new HashMap<>());
        tokenService.logoutAll(9L);
        verify(redis).delete("user:sessions:9");
        verify(redis).delete("session:s1");
        verify(redis).delete("session:s2");
    }
}
