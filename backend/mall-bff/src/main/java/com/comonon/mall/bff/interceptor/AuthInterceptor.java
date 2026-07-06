package com.comonon.mall.bff.interceptor;

import com.comonon.mall.bff.config.BffProperties;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.api.Result;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.security.RedisKeys;
import com.comonon.mall.common.security.UserContext;
import com.comonon.mall.common.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * BFF 鉴权拦截器（auth-design.md §8）。
 *
 * <p>预处理顺序：
 * <ol>
 *     <li>白名单放行</li>
 *     <li>提取 Authorization → Bearer JWT</li>
 *     <li>验签 + exp（{@link JwtUtil#parseClaims}）</li>
 *     <li>{@code EXISTS jwt:bl:{jti}} 命中即拒绝</li>
 *     <li>注入 {@code X-User-Id}、{@code X-Device-Type}（写到 request attribute，下游代理透传时读取）</li>
 * </ol>
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "X-User-Id";
    public static final String ATTR_DEVICE_TYPE = "X-Device-Type";
    public static final String ATTR_JTI = "X-Jti";
    public static final String ATTR_SID = "X-Sid";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final BffProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthInterceptor(JwtUtil jwtUtil,
                           StringRedisTemplate redisTemplate,
                           BffProperties properties) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();

        if (isWhitelisted(path)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, ErrorCode.UNAUTHORIZED, "缺少 Authorization");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (RuntimeException ex) {
            writeUnauthorized(response, ErrorCode.ACCESS_TOKEN_INVALID, "JWT 验签失败或已过期");
            return false;
        }

        String jti = claims.getId();
        if (StringUtils.hasText(jti) && Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.jwtBlacklist(jti)))) {
            writeUnauthorized(response, ErrorCode.ACCESS_TOKEN_INVALID, "access 已加入黑名单");
            return false;
        }

        String userId = claims.getSubject();
        String deviceType = claims.get("dt", String.class);
        String sid = claims.get("sid", String.class);

        if (StringUtils.hasText(sid) && !Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.session(sid)))) {
            writeUnauthorized(response, ErrorCode.ACCESS_TOKEN_INVALID, "会话不存在");
            return false;
        }

        request.setAttribute(ATTR_USER_ID, userId);
        request.setAttribute(ATTR_DEVICE_TYPE, deviceType);
        request.setAttribute(ATTR_JTI, jti);
        request.setAttribute(ATTR_SID, sid);

        if (StringUtils.hasText(userId)) {
            try {
                UserContext.set(new UserPrincipal(Long.valueOf(userId), sid, deviceType, jti));
            } catch (NumberFormatException ignore) {
                // 非数字 userId 暂不入 ThreadLocal
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = properties.getAuth().getWhitelist();
        if (whitelist == null) {
            return false;
        }
        for (String pattern : whitelist) {
            if (matchesWhitelist(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    /** 精确匹配或按路径段前缀匹配，避免 /api/products 误放行 /api/productsXYZ。 */
    static boolean matchesWhitelist(String path, String pattern) {
        if (path == null || pattern == null) {
            return false;
        }
        if (path.equals(pattern)) {
            return true;
        }
        if (!path.startsWith(pattern)) {
            return false;
        }
        return path.length() == pattern.length() || path.charAt(pattern.length()) == '/';
    }

    private void writeUnauthorized(HttpServletResponse response, int code, String detail) throws IOException {
        log.debug("BFF auth rejected: {}", detail);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> body = Result.error(code, detail);
        response.getWriter().write(mapper.writeValueAsString(body));
    }
}
