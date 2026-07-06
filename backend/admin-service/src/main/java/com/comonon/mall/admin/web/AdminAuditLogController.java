package com.comonon.mall.admin.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.admin.entity.AdminAuditLog;
import com.comonon.mall.admin.mapper.AdminAuditLogMapper;
import com.comonon.mall.admin.security.RequirePermission;
import com.comonon.mall.common.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogMapper mapper;

    @GetMapping
    @RequirePermission("admin:audit:read")
    public Result<Map<String, Object>> list(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "adminUserId", required = false) Long adminUserId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "targetId", required = false) String targetId,
            @RequestParam(value = "result", required = false) Integer result,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        int safePage = Math.min(Math.max(1, page), 10_000);
        int safeSize = Math.min(100, Math.max(1, size));

        LambdaQueryWrapper<AdminAuditLog> filter = buildFilter(action, adminUserId, username, targetId, result, from, to);
        long total = mapper.selectCount(filter);

        long offsetLong = (long) (safePage - 1) * safeSize;
        List<AdminAuditLog> list;
        if (offsetLong > Integer.MAX_VALUE || offsetLong >= total) {
            list = List.of();
        } else {
            LambdaQueryWrapper<AdminAuditLog> pageQuery = buildFilter(action, adminUserId, username, targetId, result, from, to);
            pageQuery.orderByDesc(AdminAuditLog::getId);
            pageQuery.last("LIMIT " + offsetLong + "," + safeSize);
            list = mapper.selectList(pageQuery);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("list", list);
        resp.put("total", total);
        resp.put("page", safePage);
        resp.put("size", safeSize);
        return Result.ok(resp);
    }

    @GetMapping("/export")
    @RequirePermission("admin:audit:read")
    public ResponseEntity<byte[]> export(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "adminUserId", required = false) Long adminUserId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "targetId", required = false) String targetId,
            @RequestParam(value = "result", required = false) Integer result,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LambdaQueryWrapper<AdminAuditLog> q = buildFilter(action, adminUserId, username, targetId, result, from, to);
        q.orderByDesc(AdminAuditLog::getId).last("LIMIT 5000");
        List<AdminAuditLog> list = mapper.selectList(q);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("时间,操作人,用户ID,操作,对象类型,对象ID,IP,结果,错误信息\n");
        for (AdminAuditLog row : list) {
            sb.append(csv(row.getCreatedAt() == null ? "" : row.getCreatedAt().toString())).append(',')
                    .append(csv(row.getUsername())).append(',')
                    .append(row.getAdminUserId() == null ? "" : row.getAdminUserId()).append(',')
                    .append(csv(row.getAction())).append(',')
                    .append(csv(row.getTargetType())).append(',')
                    .append(csv(row.getTargetId())).append(',')
                    .append(csv(row.getRequestIp())).append(',')
                    .append(row.getResult() == null ? "" : row.getResult()).append(',')
                    .append(csv(row.getErrorMsg()))
                    .append('\n');
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private LambdaQueryWrapper<AdminAuditLog> buildFilter(String action, Long adminUserId, String username,
                                                          String targetId, Integer result,
                                                          LocalDate from, LocalDate to) {
        LambdaQueryWrapper<AdminAuditLog> w = Wrappers.lambdaQuery();
        if (action != null && !action.isBlank()) w.eq(AdminAuditLog::getAction, action);
        if (adminUserId != null) w.eq(AdminAuditLog::getAdminUserId, adminUserId);
        if (username != null && !username.isBlank()) w.like(AdminAuditLog::getUsername, username.trim());
        if (targetId != null && !targetId.isBlank()) w.eq(AdminAuditLog::getTargetId, targetId.trim());
        if (result != null) w.eq(AdminAuditLog::getResult, result);
        if (from != null) w.ge(AdminAuditLog::getCreatedAt, from.atStartOfDay());
        if (to != null) w.lt(AdminAuditLog::getCreatedAt, to.plusDays(1).atStartOfDay());
        return w;
    }

    private static String csv(String v) {
        if (v == null) return "\"\"";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
