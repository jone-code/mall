package com.comonon.mall.user.web;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.constant.RedisKeys;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.common.security.UserContext;
import com.comonon.mall.common.security.UserPrincipal;
import com.comonon.mall.user.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器：
 * 1. 提取 Authorization → JWT
 * 2. 验签 + exp
 * 3. EXISTS jwt:bl:{jti} → 命中 401
 * 4. 注入 X-User-Id, X-Device-Type 到 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.ACCESS_TOKEN_INVALID, "缺少 access token");
        }
        String token = auth.substring(7);
        Claims claims;
        try {
            claims = tokenService.parseAccess(token);
        } catch (Exception e) {
            throw new BizException(ErrorCode.ACCESS_TOKEN_INVALID, "access token 无效");
        }
        String jti = claims.getId();
        if (jti == null || tokenService.isJtiBlacklisted(jti)) {
            throw new BizException(ErrorCode.ACCESS_TOKEN_INVALID, "access 已失效");
        }
        long userId = Long.parseLong(claims.getSubject());
        String sid = (String) claims.get("sid");
        String dt = (String) claims.get("dt");
        if (sid != null && !tokenService.isSessionAlive(sid)) {
            throw new BizException(ErrorCode.ACCESS_TOKEN_INVALID, "会话不存在");
        }
        UserContext.set(new UserPrincipal(userId, sid, dt, jti));
        // 透传给下游
        resp.setHeader("X-User-Id", String.valueOf(userId));
        if (dt != null) resp.setHeader("X-Device-Type", dt);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 仅触发 RedisKeys 类加载，避免 IDE 误删 import。 */
    @SuppressWarnings("unused")
    private static final String K = RedisKeys.SCENE_LOGIN;
}
