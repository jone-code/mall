package com.comonon.mall.admin.web;

import com.comonon.mall.admin.proxy.ProductApiProxy;
import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryProxyController {

    private final ProductApiProxy proxy;

    @GetMapping
    @RequirePermission("product:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/categories", request, null);
    }

    @PostMapping
    @RequirePermission("product:write")
    @AuditAction(value = "CREATE_CATEGORY", targetType = "category")
    public ResponseEntity<byte[]> create(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/categories", request, body);
    }

    @PutMapping("/{id}")
    @RequirePermission("product:write")
    @AuditAction(value = "UPDATE_CATEGORY", targetType = "category")
    public ResponseEntity<byte[]> update(@PathVariable("id") Long id,
                                         HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/categories/" + id, request, body);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("product:write")
    @AuditAction(value = "DELETE_CATEGORY", targetType = "category")
    public ResponseEntity<byte[]> delete(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/categories/" + id, request, null);
    }
}
