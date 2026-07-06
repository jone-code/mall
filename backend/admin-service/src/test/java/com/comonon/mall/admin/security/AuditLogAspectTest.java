package com.comonon.mall.admin.security;

import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogAspectTest {

    @Mock AuditLogService auditLogService;
    @Mock ProceedingJoinPoint pjp;

    AuditLogAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AuditLogAspect(auditLogService);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("User-Agent", "junit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        AdminContext.set(7L, "alice", java.util.List.of("ADMIN"), Set.of("admin:user:read"), "sid", "jti");
    }

    @AfterEach
    void tearDown() {
        AdminContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    private AuditAction action(String value) {
        return new AuditAction() {
            @Override public Class<? extends Annotation> annotationType() { return AuditAction.class; }
            @Override public String value() { return value; }
            @Override public String targetType() { return "x"; }
        };
    }

    @Test
    void successAction_writesLogWithResult1() throws Throwable {
        when(pjp.getArgs()).thenReturn(new Object[]{"arg1", 2});
        when(pjp.proceed()).thenReturn("ok");

        Object ret = aspect.around(pjp, action("FOO"));
        assertEquals("ok", ret);

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogService).log(cap.capture());
        AdminAuditLog log = cap.getValue();
        assertEquals("FOO", log.getAction());
        assertEquals(1, log.getResult());
        assertEquals(7L, log.getAdminUserId());
        assertEquals("alice", log.getUsername());
    }

    @Test
    void thrownAction_writesLogWithResult0_andRethrows() throws Throwable {
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> aspect.around(pjp, action("BAR")));

        ArgumentCaptor<AdminAuditLog> cap = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(auditLogService).log(cap.capture());
        AdminAuditLog log = cap.getValue();
        assertEquals("BAR", log.getAction());
        assertEquals(0, log.getResult());
        assertEquals("boom", log.getErrorMsg());
    }
}
