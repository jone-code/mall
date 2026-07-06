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
public class AdminOrderProxyController {

    private final OrderApiProxy proxy;

    @GetMapping("/orders/trends")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> trends(HttpServletRequest request) {
        return proxy.forward("/admin/orders/trends", request, null);
    }

    @GetMapping("/orders/export")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> export(HttpServletRequest request) {
        return proxy.forward("/admin/orders/export", request, null);
    }

    @PostMapping("/orders/batch-ship")
    @RequirePermission("order:write")
    @AuditAction(value = "BATCH_SHIP", targetType = "order")
    public ResponseEntity<byte[]> batchShip(@RequestBody(required = false) byte[] body,
                                            HttpServletRequest request) {
        return proxy.forward("/admin/orders/batch-ship", request, body);
    }

    @GetMapping("/orders/stats")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> stats(HttpServletRequest request) {
        return proxy.forward("/admin/orders/stats", request, null);
    }

    @GetMapping("/orders")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/orders", request, null);
    }

    @GetMapping("/orders/{orderNo}")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> detail(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo, request, null);
    }

    @PostMapping("/orders/{orderNo}/close")
    @RequirePermission("order:write")
    @AuditAction(value = "CLOSE_ORDER", targetType = "order")
    public ResponseEntity<byte[]> close(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/close", request, null);
    }

    @PostMapping("/orders/{orderNo}/ship")
    @RequirePermission("order:write")
    @AuditAction(value = "SHIP_ORDER", targetType = "order")
    public ResponseEntity<byte[]> ship(@PathVariable("orderNo") String orderNo,
                                       @RequestBody(required = false) byte[] body,
                                       HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/ship", request, body);
    }

    @PutMapping("/orders/{orderNo}/remark")
    @RequirePermission("order:write")
    public ResponseEntity<byte[]> remark(@PathVariable("orderNo") String orderNo,
                                         @RequestBody(required = false) byte[] body,
                                         HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/remark", request, body);
    }

    @PostMapping("/orders/{orderNo}/refund/approve")
    @RequirePermission("order:write")
    @AuditAction(value = "APPROVE_REFUND", targetType = "order")
    public ResponseEntity<byte[]> approveRefund(@PathVariable("orderNo") String orderNo,
                                                HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/refund/approve", request, null);
    }

    @PostMapping("/orders/{orderNo}/refund/reject")
    @RequirePermission("order:write")
    @AuditAction(value = "REJECT_REFUND", targetType = "order")
    public ResponseEntity<byte[]> rejectRefund(@PathVariable("orderNo") String orderNo,
                                               HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/refund/reject", request, null);
    }

    @GetMapping("/orders/{orderNo}/logistics")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> logistics(@PathVariable("orderNo") String orderNo,
                                          HttpServletRequest request) {
        return proxy.forward("/admin/orders/" + orderNo + "/logistics", request, null);
    }
}
