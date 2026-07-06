package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.mapper.AdminAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 异步审计日志写入。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AdminAuditLogMapper mapper;

    @Async
    public void log(AdminAuditLog log) {
        try {
            if (log.getCreatedAt() == null) {
                log.setCreatedAt(LocalDateTime.now());
            }
            if (log.getResult() == null) {
                log.setResult(1);
            }
            mapper.insert(log);
        } catch (Exception e) {
            AuditLogService.log.error("write admin_audit_log failed: action={}, err={}", log.getAction(), e.getMessage(), e);
        }
    }

    public void logSync(AdminAuditLog l) {
        if (l.getCreatedAt() == null) l.setCreatedAt(LocalDateTime.now());
        if (l.getResult() == null) l.setResult(1);
        mapper.insert(l);
    }
}
