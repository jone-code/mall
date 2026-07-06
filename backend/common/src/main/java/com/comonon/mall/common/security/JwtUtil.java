package com.comonon.mall.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具：HS256，密钥从 mall.jwt.secret 注入。
 * 同时提供两套 API：
 *  - C 端用 sign(userId, sid, deviceType) -> SignResult
 *  - 管理端 / BFF 用 issue(sub, ttlMs, claims) -> TokenPair
 *  payload 字段: sub / sid / dt / iat / exp / jti / iss
 */
public class JwtUtil {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final String issuer;

    /** C 端构造：固定 access TTL。 */
    public JwtUtil(String secret, Duration accessTtl) {
        this(secret, accessTtl, "mall");
    }

    /** 管理端 / BFF 构造：仅指定 issuer，TTL 在调用方传入。 */
    public JwtUtil(String secret, String issuer) {
        this(secret, Duration.ZERO, issuer);
    }

    public JwtUtil(String secret, Duration accessTtl, String issuer) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("mall.jwt.secret 长度必须 >=32 字节(HS256)");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtl == null ? 0L : accessTtl.getSeconds();
        this.issuer = issuer == null ? "mall" : issuer;
    }

    /* ===================== C 端 API ===================== */

    public SignResult sign(long userId, String sid, String deviceType) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);
        String jti = UUID.randomUUID().toString().replace("-", "");
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .claim("sid", sid)
                .claim("dt", deviceType)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new SignResult(token, jti, exp.getEpochSecond());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean verify(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    /* ===================== 管理端 / BFF API ===================== */

    /**
     * 通用签发：指定 subject、TTL（毫秒）、附加 claims。
     */
    public TokenPair issue(String subject, long ttlMs, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMs);
        String jti = UUID.randomUUID().toString().replace("-", "");
        var builder = Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp));
        Map<String, Object> claims = extraClaims == null ? new HashMap<>() : new HashMap<>(extraClaims);
        for (Map.Entry<String, Object> e : claims.entrySet()) {
            builder.claim(e.getKey(), e.getValue());
        }
        String token = builder.signWith(key, Jwts.SIG.HS256).compact();
        return new TokenPair(token, jti, exp.getEpochSecond());
    }

    /** parseClaims 的别名，保持调用方代码语义。 */
    public Claims parse(String token) {
        return parseClaims(token);
    }

    @Data
    public static class SignResult {
        private final String token;
        private final String jti;
        private final long exp;
    }

    /** 管理端 / BFF 使用的签发结果。 */
    public record TokenPair(String token, String jti, long exp) {
    }
}
