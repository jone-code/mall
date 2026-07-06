package com.comonon.mall.bff.interceptor;

import com.comonon.mall.bff.config.BffProperties;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.security.RedisKeys;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * AuthInterceptor 单元测试，覆盖：
 * <ul>
 *     <li>白名单放行</li>
 *     <li>缺少 Authorization → 401</li>
 *     <li>JWT 解析失败 → 401</li>
 *     <li>命中黑名单 → 401</li>
 *     <li>合法 token → 通过 + 注入 X-User-Id / X-Device-Type</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthInterceptorTest {

    private static final String SECRET = "unit-test-secret-32bytes-minimum-aaaaaa";
    private final JwtUtil jwtUtil = new JwtUtil(SECRET, Duration.ofMinutes(5));

    @Mock
    private StringRedisTemplate redisTemplate;

    private BffProperties properties;
    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        properties = new BffProperties();
        properties.getAuth().setJwtSecret(SECRET);
        properties.getAuth().setWhitelist(List.of(
                "/api/sms/send",
                "/api/login/sms",
                "/api/oauth/wechat",
                "/api/token/refresh",
                "/api/captcha/verify"
        ));
        interceptor = new AuthInterceptor(jwtUtil, redisTemplate, properties);
    }

    @Test
    void whitelistPathPassesWithoutAuthHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/sms/send");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean ok = interceptor.preHandle(req, resp, new Object());

        assertTrue(ok);
        assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
    }

    @Test
    void missingAuthorizationHeaderRejected() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean ok = interceptor.preHandle(req, resp, new Object());

        assertFalse(ok);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
    }

    @Test
    void invalidJwtRejected() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
        req.addHeader("Authorization", "Bearer not-a-real-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean ok = interceptor.preHandle(req, resp, new Object());

        assertFalse(ok);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
    }

    @Test
    void blacklistedJtiRejected() throws Exception {
        JwtUtil.SignResult signed = jwtUtil.sign(10086L, "sess_x", "APP_IOS");

        when(redisTemplate.hasKey(RedisKeys.jwtBlacklist(signed.getJti()))).thenReturn(Boolean.TRUE);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
        req.addHeader("Authorization", "Bearer " + signed.getToken());
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean ok = interceptor.preHandle(req, resp, new Object());

        assertFalse(ok);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
    }

    @Test
    void validJwtPassesAndInjectsAttributes() throws Exception {
        JwtUtil.SignResult signed = jwtUtil.sign(10086L, "sess_x", "APP_IOS");

        lenient().when(redisTemplate.hasKey(RedisKeys.jwtBlacklist(signed.getJti()))).thenReturn(Boolean.FALSE);
        lenient().when(redisTemplate.hasKey(RedisKeys.session("sess_x"))).thenReturn(Boolean.TRUE);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/me");
        req.addHeader("Authorization", "Bearer " + signed.getToken());
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean ok = interceptor.preHandle(req, resp, new Object());

        assertTrue(ok);
        assertEquals("10086", req.getAttribute(AuthInterceptor.ATTR_USER_ID));
        assertEquals("APP_IOS", req.getAttribute(AuthInterceptor.ATTR_DEVICE_TYPE));
        assertEquals(signed.getJti(), req.getAttribute(AuthInterceptor.ATTR_JTI));

        Claims parsed = jwtUtil.parseClaims(signed.getToken());
        assertEquals("10086", parsed.getSubject());
    }

    @Test
    void whitelistPrefixDoesNotMatchSimilarPath() {
        properties.getAuth().setWhitelist(new java.util.ArrayList<>(List.of(
                "/api/sms/send",
                "/api/login/sms",
                "/api/oauth/wechat",
                "/api/token/refresh",
                "/api/captcha/verify",
                "/api/products"
        )));
        assertTrue(AuthInterceptor.matchesWhitelist("/api/products", "/api/products"));
        assertTrue(AuthInterceptor.matchesWhitelist("/api/products/123", "/api/products"));
        assertFalse(AuthInterceptor.matchesWhitelist("/api/productsXYZ", "/api/products"));
    }
}
