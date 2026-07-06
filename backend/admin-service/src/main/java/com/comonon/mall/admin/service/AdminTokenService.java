package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.vo.AdminSessionVO;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.security.RedisKeys;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 管理后台 token 服务：access (JWT)+ refresh (opaque)，同账号互踢，refresh rotation。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTokenService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final AuditLogService auditLogService;

    @Value("${mall.jwt.access-ttl-seconds:1800}")
    private long accessTtlSeconds;

    @Value("${mall.jwt.refresh-ttl-seconds:86400}")
    private long refreshTtlSeconds;

    /**
     * 互踢同账号其它会话：删 session、加黑 jti。
     */
    public void revokeAllSessions(Long adminUserId) {
        String setKey = RedisKeys.adminUserSessions(adminUserId);
        Set<String> sids = redis.opsForSet().members(setKey);
        if (sids == null) return;
        for (String sid : sids) {
            String sessionKey = RedisKeys.session(sid);
            Map<Object, Object> hash = redis.opsForHash().entries(sessionKey);
            if (hash != null && !hash.isEmpty()) {
                Object accessJti = hash.get("accessJti");
                if (accessJti != null) {
                    redis.opsForValue().set(RedisKeys.jwtBlacklist(accessJti.toString()), "1",
                            Duration.ofSeconds(accessTtlSeconds));
                }
            }
            redis.delete(sessionKey);
            redis.opsForSet().remove(setKey, sid);
        }
    }

    /**
     * 签发 access + refresh；建立 session。
     */
    public IssuedTokens issue(Long adminUserId, String username, List<String> roles, String permsHash) {
        String sid = "sess_" + UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sid", sid);
        claims.put("role", "ADMIN");
        claims.put("roles", roles == null ? List.of() : roles);
        claims.put("permsHash", permsHash);
        claims.put("username", username);

        JwtUtil.TokenPair access = jwtUtil.issue(String.valueOf(adminUserId), accessTtlSeconds * 1000L, claims);
        String refreshToken = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        long permsVersion = ensureVersion(adminUserId);

        // session:{sid}
        String sessionKey = RedisKeys.session(sid);
        Map<String, String> sessionHash = new HashMap<>();
        sessionHash.put("adminUserId", String.valueOf(adminUserId));
        sessionHash.put("username", username);
        sessionHash.put("refreshToken", refreshToken);
        sessionHash.put("accessJti", access.jti());
        sessionHash.put("permsVersion", String.valueOf(permsVersion));
        sessionHash.put("createdAt", String.valueOf(System.currentTimeMillis()));
        sessionHash.put("lastActiveAt", String.valueOf(System.currentTimeMillis()));
        redis.opsForHash().putAll(sessionKey, sessionHash);
        redis.expire(sessionKey, Duration.ofSeconds(refreshTtlSeconds));

        // user:sessions:{userId}
        redis.opsForSet().add(RedisKeys.adminUserSessions(adminUserId), sid);

        return new IssuedTokens(access.token(), refreshToken, sid, access.jti(), accessTtlSeconds, refreshTtlSeconds);
    }

    private long ensureVersion(Long adminUserId) {
        String key = RedisKeys.adminPermsVersion(adminUserId);
        String v = redis.opsForValue().get(key);
        if (v == null) {
            redis.opsForValue().setIfAbsent(key, "0");
            return 0L;
        }
        return Long.parseLong(v);
    }

    /**
     * 刷新：校验 refresh，rotation 旧 refresh，签新 access；session TTL 重置。
     */
    public IssuedTokens refresh(String accessTokenForSid, String refreshToken, String ip, String userAgent) {
        Claims claims;
        try {
            claims = jwtUtil.parse(accessTokenForSid);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.REFRESH_INVALID, "access 无法解析");
        }
        String sid = claims.get("sid", String.class);
        if (sid == null) throw new BusinessException(ErrorCodes.REFRESH_INVALID, "sid missing");
        String sessionKey = RedisKeys.session(sid);

        Object stored = redis.opsForHash().get(sessionKey, "refreshToken");
        if (stored == null) throw new BusinessException(ErrorCodes.REFRESH_INVALID, "session 不存在");
        if (!stored.equals(refreshToken)) {
            // rotation 异常：踢下当前 sid + 加黑当前 access
            Long adminUserId = Long.valueOf((String) redis.opsForHash().get(sessionKey, "adminUserId"));
            Object accessJti = redis.opsForHash().get(sessionKey, "accessJti");
            if (accessJti != null) {
                redis.opsForValue().set(RedisKeys.jwtBlacklist(accessJti.toString()), "1",
                        Duration.ofSeconds(accessTtlSeconds));
            }
            redis.delete(sessionKey);
            redis.opsForSet().remove(RedisKeys.adminUserSessions(adminUserId), sid);
            throw new BusinessException(ErrorCodes.REFRESH_INVALID, "refresh rotation 异常");
        }

        Long adminUserId = Long.valueOf((String) redis.opsForHash().get(sessionKey, "adminUserId"));
        String username = (String) redis.opsForHash().get(sessionKey, "username");

        // 旧 access 黑名单
        Object oldJti = redis.opsForHash().get(sessionKey, "accessJti");
        if (oldJti != null) {
            redis.opsForValue().set(RedisKeys.jwtBlacklist(oldJti.toString()), "1",
                    Duration.ofSeconds(accessTtlSeconds));
        }

        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("sid", sid);
        newClaims.put("role", "ADMIN");
        newClaims.put("username", username);
        // 重新读 permsHash 由调用方决定；此处保留旧值
        Object permsHash = redis.opsForHash().get(sessionKey, "permsHash");
        if (permsHash != null) newClaims.put("permsHash", permsHash);

        JwtUtil.TokenPair newAccess = jwtUtil.issue(String.valueOf(adminUserId), accessTtlSeconds * 1000L, newClaims);
        String newRefresh = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        redis.opsForHash().put(sessionKey, "refreshToken", newRefresh);
        redis.opsForHash().put(sessionKey, "accessJti", newAccess.jti());
        redis.opsForHash().put(sessionKey, "lastActiveAt", String.valueOf(System.currentTimeMillis()));
        redis.expire(sessionKey, Duration.ofSeconds(refreshTtlSeconds));

        AdminAuditLog audit = new AdminAuditLog();
        audit.setAdminUserId(adminUserId);
        audit.setUsername(username);
        audit.setAction("TOKEN_REFRESH");
        audit.setTargetType("admin_user");
        audit.setTargetId(String.valueOf(adminUserId));
        audit.setRequestIp(ip);
        audit.setUserAgent(userAgent);
        audit.setResult(1);
        audit.setCreatedAt(LocalDateTime.now());
        auditLogService.log(audit);

        return new IssuedTokens(newAccess.token(), newRefresh, sid, newAccess.jti(), accessTtlSeconds, refreshTtlSeconds);
    }

    /**
     * 登出：删 session + 加黑 access jti。
     */
    public void logout(Long adminUserId, String sid, String jti, long remainingTtlSec) {
        if (jti != null && remainingTtlSec > 0) {
            redis.opsForValue().set(RedisKeys.jwtBlacklist(jti), "1", Duration.ofSeconds(remainingTtlSec));
        }
        redis.delete(RedisKeys.session(sid));
        redis.opsForSet().remove(RedisKeys.adminUserSessions(adminUserId), sid);
    }

    public Set<String> listSessions(Long adminUserId) {
        Set<String> set = redis.opsForSet().members(RedisKeys.adminUserSessions(adminUserId));
        return set == null ? new HashSet<>() : set;
    }

    public List<AdminSessionVO> listSessionDetails(Long adminUserId, String currentSid) {
        List<AdminSessionVO> result = new ArrayList<>();
        for (String sid : listSessions(adminUserId)) {
            Map<Object, Object> hash = redis.opsForHash().entries(RedisKeys.session(sid));
            if (hash == null || hash.isEmpty()) {
                continue;
            }
            AdminSessionVO vo = new AdminSessionVO();
            vo.setSid(sid);
            vo.setCreatedAt(parseEpoch(hash.get("createdAt")));
            vo.setLastActiveAt(parseEpoch(hash.get("lastActiveAt")));
            vo.setCurrent(sid.equals(currentSid));
            result.add(vo);
        }
        result.sort((a, b) -> Long.compare(
                b.getLastActiveAt() == null ? 0L : b.getLastActiveAt(),
                a.getLastActiveAt() == null ? 0L : a.getLastActiveAt()));
        return result;
    }

    public void kickSession(Long adminUserId, String sid, String currentSid) {
        if (sid != null && sid.equals(currentSid)) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "不能踢出当前会话，请使用退出登录");
        }
        String sessionKey = RedisKeys.session(sid);
        Map<Object, Object> hash = redis.opsForHash().entries(sessionKey);
        if (hash == null || hash.isEmpty()) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "会话不存在或已失效");
        }
        Object uid = hash.get("adminUserId");
        if (uid == null || !String.valueOf(adminUserId).equals(uid.toString())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "无权操作该会话");
        }
        Object accessJti = hash.get("accessJti");
        if (accessJti != null) {
            redis.opsForValue().set(RedisKeys.jwtBlacklist(accessJti.toString()), "1",
                    Duration.ofSeconds(accessTtlSeconds));
        }
        redis.delete(sessionKey);
        redis.opsForSet().remove(RedisKeys.adminUserSessions(adminUserId), sid);
    }

    private static Long parseEpoch(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Data
    public static class IssuedTokens {
        private final String accessToken;
        private final String refreshToken;
        private final String sid;
        private final String jti;
        private final long accessTtlSec;
        private final long refreshTtlSec;
    }
}
