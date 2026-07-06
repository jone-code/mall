package com.comonon.mall.user.service;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.constant.RedisKeys;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.user.config.TokenProps;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Token 与 Session 管理：
 * - access JWT (2h) + refresh 不透明 UUID (7d 滑动)
 * - session:{sid} hash; user:sessions:{userId} set; user:dev:{userId}:{deviceType} 单值
 * - refresh rotation + 重放检测
 * - jwt:bl:{jti} 黑名单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String F_USER_ID = "userId";
    public static final String F_DEVICE_ID = "deviceId";
    public static final String F_DEVICE_TYPE = "deviceType";
    public static final String F_REFRESH = "refreshToken";
    public static final String F_ACCESS_JTI = "accessJti";
    public static final String F_ACCESS_EXP = "accessExp";
    public static final String F_CREATED_AT = "createdAt";
    public static final String F_LAST_ACTIVE_AT = "lastActiveAt";
    public static final String F_PREV_REFRESH = "prevRefreshToken";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final TokenProps tokenProps;

    /**
     * 签发 access + refresh，并完成同 deviceType 互踢。
     */
    public TokenPair issue(long userId, String deviceId, String deviceType) {
        // 互踢同 deviceType 旧会话
        String devKey = RedisKeys.userDevice(userId, deviceType);
        String oldSid = redis.opsForValue().get(devKey);
        if (oldSid != null) {
            kickSession(userId, oldSid);
        }

        String sid = "sess_" + UUID.randomUUID().toString().replace("-", "");
        JwtUtil.SignResult sign = jwtUtil.sign(userId, sid, deviceType);
        String refresh = UUID.randomUUID().toString().replace("-", "");
        long now = Instant.now().getEpochSecond();

        Map<String, String> session = new HashMap<>();
        session.put(F_USER_ID, String.valueOf(userId));
        session.put(F_DEVICE_ID, deviceId == null ? "" : deviceId);
        session.put(F_DEVICE_TYPE, deviceType);
        session.put(F_REFRESH, refresh);
        session.put(F_ACCESS_JTI, sign.getJti());
        session.put(F_ACCESS_EXP, String.valueOf(sign.getExp()));
        session.put(F_CREATED_AT, String.valueOf(now));
        session.put(F_LAST_ACTIVE_AT, String.valueOf(now));

        String sessionKey = RedisKeys.session(sid);
        redis.opsForHash().putAll(sessionKey, session);
        redis.expire(sessionKey, tokenProps.getRefreshTtl());

        redis.opsForSet().add(RedisKeys.userSessions(userId), sid);
        redis.expire(RedisKeys.userSessions(userId), tokenProps.getRefreshTtl());

        redis.opsForValue().set(devKey, sid, tokenProps.getRefreshTtl());

        return new TokenPair(sign.getToken(), refresh, sid, sign.getExp());
    }

    /**
     * 刷新：refresh rotation + 重放检测。
     */
    public TokenPair refresh(String accessOrSidHint, String refreshToken) {
        // 客户端只送 refreshToken；我们必须从 refreshToken 找到 sid。
        // 简化方案：要求客户端同时带上 sid（解析旧 access 的 claims）。这里支持两路：
        // 1) 调用方传 sidHint（已解析）；2) 通过遍历不可行，因此本方法需要 sid
        if (accessOrSidHint == null || accessOrSidHint.isEmpty()) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID, "缺少会话标识");
        }
        String sid = accessOrSidHint;
        String sessionKey = RedisKeys.session(sid);
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        if (session.isEmpty()) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID, "会话不存在");
        }
        String currentRefresh = (String) session.get(F_REFRESH);
        String prevRefresh = (String) session.get(F_PREV_REFRESH);
        long userId = Long.parseLong((String) session.get(F_USER_ID));
        String deviceType = (String) session.get(F_DEVICE_TYPE);

        // 重放检测：若提交的是上一次旋转前的 refresh，判定盗用
        if (prevRefresh != null && prevRefresh.equals(refreshToken)) {
            kickSession(userId, sid);
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh 重放，强制下线");
        }
        if (currentRefresh == null || !currentRefresh.equals(refreshToken)) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID, "refreshToken 无效");
        }

        // 绝对上限
        long createdAt = Long.parseLong((String) session.getOrDefault(F_CREATED_AT, "0"));
        if (createdAt > 0
                && Instant.now().getEpochSecond() - createdAt > tokenProps.getAbsoluteMax().toSeconds()) {
            kickSession(userId, sid);
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID, "登录态已达绝对上限");
        }

        // 旧 access 加黑
        String oldJti = (String) session.get(F_ACCESS_JTI);
        String oldExpStr = (String) session.get(F_ACCESS_EXP);
        blacklistJti(oldJti, oldExpStr);

        // rotate
        JwtUtil.SignResult sign = jwtUtil.sign(userId, sid, deviceType);
        String newRefresh = UUID.randomUUID().toString().replace("-", "");
        long now = Instant.now().getEpochSecond();

        redis.opsForHash().put(sessionKey, F_PREV_REFRESH, currentRefresh);
        redis.opsForHash().put(sessionKey, F_REFRESH, newRefresh);
        redis.opsForHash().put(sessionKey, F_ACCESS_JTI, sign.getJti());
        redis.opsForHash().put(sessionKey, F_ACCESS_EXP, String.valueOf(sign.getExp()));
        redis.opsForHash().put(sessionKey, F_LAST_ACTIVE_AT, String.valueOf(now));
        redis.expire(sessionKey, tokenProps.getRefreshTtl());
        redis.expire(RedisKeys.userSessions(userId), tokenProps.getRefreshTtl());
        redis.expire(RedisKeys.userDevice(userId, deviceType), tokenProps.getRefreshTtl());

        return new TokenPair(sign.getToken(), newRefresh, sid, sign.getExp());
    }

    /**
     * 登出当前会话：删除 session、移除 set、当前 access 加黑名单。
     */
    public void logout(long userId, String sid, String currentJti, long currentExp) {
        kickSession(userId, sid);
        blacklistJti(currentJti, String.valueOf(currentExp));
    }

    public void logoutAll(long userId) {
        Set<String> sids = redis.opsForSet().members(RedisKeys.userSessions(userId));
        if (sids != null) {
            for (String sid : sids) {
                kickSession(userId, sid);
            }
        }
        redis.delete(RedisKeys.userSessions(userId));
    }

    /**
     * 踢下指定 sid 会话：旧 access jti 加黑、SREM、del session、清理 dev key。
     */
    public void kickSession(long userId, String sid) {
        String sessionKey = RedisKeys.session(sid);
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        if (!session.isEmpty()) {
            Object uidObj = session.get(F_USER_ID);
            if (uidObj != null) {
                long sessionUserId = Long.parseLong(uidObj.toString());
                if (sessionUserId != userId) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "无权操作该会话");
                }
            }
            String jti = (String) session.get(F_ACCESS_JTI);
            String expStr = (String) session.get(F_ACCESS_EXP);
            blacklistJti(jti, expStr);

            String dt = (String) session.get(F_DEVICE_TYPE);
            if (dt != null) {
                String devKey = RedisKeys.userDevice(userId, dt);
                String curSid = redis.opsForValue().get(devKey);
                if (sid.equals(curSid)) {
                    redis.delete(devKey);
                }
            }
        }
        redis.opsForSet().remove(RedisKeys.userSessions(userId), sid);
        redis.delete(sessionKey);
    }

    public boolean isJtiBlacklisted(String jti) {
        Boolean ex = redis.hasKey(RedisKeys.jwtBlacklist(jti));
        return Boolean.TRUE.equals(ex);
    }

    public boolean isSessionAlive(String sid) {
        Boolean ex = redis.hasKey(RedisKeys.session(sid));
        return Boolean.TRUE.equals(ex);
    }

    public Map<Object, Object> getSession(String sid) {
        return redis.opsForHash().entries(RedisKeys.session(sid));
    }

    public Set<String> listSessionIds(long userId) {
        return redis.opsForSet().members(RedisKeys.userSessions(userId));
    }

    public Claims parseAccess(String token) {
        return jwtUtil.parseClaims(token);
    }

    private void blacklistJti(String jti, String expStr) {
        if (jti == null || expStr == null) return;
        try {
            long exp = Long.parseLong(expStr);
            long ttl = exp - Instant.now().getEpochSecond();
            if (ttl > 0) {
                redis.opsForValue().set(RedisKeys.jwtBlacklist(jti), "1", Duration.ofSeconds(ttl));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Data
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final String sid;
        private final long accessExp;
    }
}
