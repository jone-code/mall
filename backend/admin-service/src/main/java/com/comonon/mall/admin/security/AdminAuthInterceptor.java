package com.comonon.mall.admin.security;

import com.comonon.mall.admin.service.PermissionService;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.security.RedisKeys;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.List;

/**
 * Admin 鉴权拦截器：
 *  1. 解析 access JWT；
 *  2. 黑名单检查；
 *  3. session 是否存在 + permsVersion 比对；
 *  4. 注入 AdminContext + 响应头 X-Admin-User-Id / X-Admin-Roles。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "缺少 access token");
        }
        String token = auth.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "access 无效");
        }

        String jti = claims.getId();
        String sid = claims.get("sid", String.class);
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "非管理端 token");
        }
        if (Boolean.TRUE.equals(redis.hasKey(RedisKeys.jwtBlacklist(jti)))) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "access 已被吊销");
        }
        Long adminUserId = Long.valueOf(claims.getSubject());
        String sessionKey = RedisKeys.session(sid);
        if (!Boolean.TRUE.equals(redis.hasKey(sessionKey))) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "session 不存在");
        }

        // permsVersion 比对：当前用户最新版本 vs session 上的版本
        long current = permissionService.currentVersion(adminUserId);
        Object sessionVersion = redis.opsForHash().get(sessionKey, "permsVersion");
        long sessionVer = sessionVersion == null ? 0L : Long.parseLong(sessionVersion.toString());
        if (current != sessionVer) {
            throw new BusinessException(ErrorCodes.PERMS_VERSION_STALE, "权限已变更，请重新登录");
        }

        // 加载权限点（每次拉 DB；如有性能问题可加进程内缓存）
        List<String> codes = permissionService.loadPermissionCodes(adminUserId);
        List<String> roles = permissionService.loadRoleCodes(adminUserId);

        String username = claims.get("username", String.class);
        AdminContext.set(adminUserId, username, roles, new HashSet<>(codes), sid, jti);

        response.setHeader("X-Admin-User-Id", String.valueOf(adminUserId));
        response.setHeader("X-Admin-Roles", String.join(",", roles));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AdminContext.clear();
    }
}
