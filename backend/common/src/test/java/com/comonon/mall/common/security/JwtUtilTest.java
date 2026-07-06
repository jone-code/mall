package com.comonon.mall.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwt = new JwtUtil(
            "0123456789abcdef0123456789abcdef0123456789abcdef",
            Duration.ofHours(2));

    @Test
    void signAndParse() {
        JwtUtil.SignResult r = jwt.sign(10086L, "sess_abc", "APP_IOS");
        assertNotNull(r.getToken());
        Claims c = jwt.parseClaims(r.getToken());
        assertEquals("10086", c.getSubject());
        assertEquals("sess_abc", c.get("sid"));
        assertEquals("APP_IOS", c.get("dt"));
        assertEquals(r.getJti(), c.getId());
    }

    @Test
    void verifyOk() {
        JwtUtil.SignResult r = jwt.sign(1L, "s", "H5");
        assertTrue(jwt.verify(r.getToken()));
    }

    @Test
    void verifyFail() {
        assertFalse(jwt.verify("not-a-token"));
    }

    @Test
    void shortSecretRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtUtil("short", Duration.ofHours(1)));
    }
}
