package com.comonon.mall.admin.security;

import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.service.AuditLogService;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计切面：被 @AuditAction 注解的方法在执行前后异步落 admin_audit_log。
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper = JsonMapperFactory.create();

    @Pointcut("@annotation(action)")
    public void audit(AuditAction action) {}

    @Around(value = "audit(action)", argNames = "pjp,action")
    public Object around(ProceedingJoinPoint pjp, AuditAction action) throws Throwable {
        AdminAuditLog log = new AdminAuditLog();
        log.setAction(action.value());
        log.setTargetType(action.targetType());
        log.setCreatedAt(LocalDateTime.now());

        String targetId = AuditBodySanitizer.extractTargetId(pjp);
        if (StringUtils.hasText(targetId)) {
            log.setTargetId(targetId);
        }

        AdminContext.Holder ctx = AdminContext.get();
        if (ctx != null) {
            log.setAdminUserId(ctx.adminUserId());
            log.setUsername(ctx.username());
        }
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            log.setRequestIp(clientIp(req));
            log.setUserAgent(req.getHeader("User-Agent"));
        }
        log.setRequestBody(AuditBodySanitizer.sanitizeArgs(pjp, objectMapper));
        try {
            Object ret = pjp.proceed();
            log.setResult(1);
            auditLogService.log(log);
            return ret;
        } catch (Throwable t) {
            log.setResult(0);
            log.setErrorMsg(t.getMessage());
            auditLogService.log(log);
            throw t;
        }
    }

    public static String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) return h.split(",")[0].trim();
        h = req.getHeader("X-Real-IP");
        if (h != null && !h.isBlank()) return h;
        return req.getRemoteAddr();
    }
}
