package com.comonon.mall.admin.security;

import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AuditBodySanitizerTest {

    private final ObjectMapper mapper = JsonMapperFactory.create();

    @Test
    void sanitizeArgs_redactsSensitiveFields() {
        var pjp = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint.class);
        Mockito.when(pjp.getArgs()).thenReturn(new Object[]{
                java.util.Map.of("username", "admin", "password", "Secret123")
        });

        String body = AuditBodySanitizer.sanitizeArgs(pjp, mapper);
        assertTrue(body.contains("admin"));
        assertTrue(body.contains("[REDACTED]"));
        assertFalse(body.contains("Secret123"));
    }

    @Test
    void sanitizeArgs_skipsServletRequest() {
        var pjp = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint.class);
        Mockito.when(pjp.getArgs()).thenReturn(new Object[]{
                new org.springframework.mock.web.MockHttpServletRequest(),
                "order-001"
        });

        String body = AuditBodySanitizer.sanitizeArgs(pjp, mapper);
        assertTrue(body.contains("order-001"));
        assertFalse(body.contains("MockHttpServletRequest"));
    }
}
