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
public class AdminVirtualCardProxyController {

    private final OrderApiProxy proxy;

    @GetMapping("/virtual-cards")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/virtual-cards", request, null);
    }

    @GetMapping("/virtual-cards/pool-summary")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> poolSummary(HttpServletRequest request) {
        return proxy.forward("/admin/virtual-cards/pool-summary", request, null);
    }

    @GetMapping("/virtual-cards/stats")
    @RequirePermission("card:read")
    public ResponseEntity<byte[]> stats(HttpServletRequest request) {
        return proxy.forward("/admin/virtual-cards/stats", request, null);
    }

    @PostMapping("/virtual-cards/import")
    @RequirePermission("card:import")
    @AuditAction(value = "IMPORT_CARDS", targetType = "virtual_card")
    public ResponseEntity<byte[]> importCards(@RequestBody(required = false) byte[] body,
                                              HttpServletRequest request) {
        return proxy.forward("/admin/virtual-cards/import", request, body);
    }

    @PutMapping("/virtual-cards/{id}")
    @RequirePermission("card:import")
    @AuditAction(value = "UPDATE_CARD", targetType = "virtual_card")
    public ResponseEntity<byte[]> update(@PathVariable("id") Long id,
                                         @RequestBody(required = false) byte[] body,
                                         HttpServletRequest request) {
        return proxy.forward("/admin/virtual-cards/" + id, request, body);
    }
}
