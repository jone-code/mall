package com.comonon.mall.admin.web;

import com.comonon.mall.admin.proxy.OrderApiProxy;
import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminServiceVerifyCodeProxyController {

    private final OrderApiProxy proxy;

    @GetMapping("/service-verify-codes")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/service-verify-codes", request, null);
    }

    @GetMapping("/service-verify-codes/stats")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> stats(HttpServletRequest request) {
        return proxy.forward("/admin/service-verify-codes/stats", request, null);
    }

    @GetMapping("/service-verify-codes/pool-summary")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> poolSummary(HttpServletRequest request) {
        return proxy.forward("/admin/service-verify-codes/pool-summary", request, null);
    }

    @PostMapping("/service-verify-codes/import")
    @RequirePermission("card:import")
    @AuditAction(value = "IMPORT_SERVICE_CODES", targetType = "service_verify_code")
    public ResponseEntity<byte[]> importCodes(@RequestBody(required = false) byte[] body,
                                              HttpServletRequest request) {
        return proxy.forward("/admin/service-verify-codes/import", request, body);
    }

    @PutMapping("/service-verify-codes/{id}")
    @RequirePermission("card:import")
    @AuditAction(value = "UPDATE_SERVICE_CODE", targetType = "service_verify_code")
    public ResponseEntity<byte[]> update(@PathVariable("id") Long id,
                                         @RequestBody(required = false) byte[] body,
                                         HttpServletRequest request) {
        return proxy.forward("/admin/service-verify-codes/" + id, request, body);
    }

    @PostMapping("/service/verify")
    @RequirePermission("order:write")
    @AuditAction(value = "VERIFY_SERVICE", targetType = "order")
    public ResponseEntity<byte[]> verify(@RequestBody(required = false) byte[] body,
                                         HttpServletRequest request) {
        return proxy.forward("/admin/service/verify", request, body);
    }
}
