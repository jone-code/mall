package com.comonon.mall.admin.web;

import com.comonon.mall.admin.proxy.OrderApiProxy;
import com.comonon.mall.admin.security.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ops")
@RequiredArgsConstructor
public class AdminOpsProxyController {

    private final OrderApiProxy proxy;

    @GetMapping("/todos")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> todos(HttpServletRequest request) {
        return proxy.forward("/admin/ops/todos", request, null);
    }
}
