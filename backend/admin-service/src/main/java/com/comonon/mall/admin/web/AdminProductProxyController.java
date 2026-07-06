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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductProxyController {

    private final ProductApiProxy proxy;

    @GetMapping("/products/stats")
    @RequirePermission("product:read")
    public ResponseEntity<byte[]> productStats(HttpServletRequest request) {
        return proxy.forward("/admin/products/stats", request, null);
    }

    @GetMapping("/products")
    @RequirePermission("product:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/products", request, null);
    }

    @GetMapping("/products/{id}")
    @RequirePermission("product:read")
    public ResponseEntity<byte[]> detail(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/products/" + id, request, null);
    }

    @PostMapping("/products")
    @RequirePermission("product:write")
    @AuditAction(value = "CREATE_SPU", targetType = "spu")
    public ResponseEntity<byte[]> create(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/products", request, body);
    }

    @PutMapping("/products/{id}")
    @RequirePermission("product:write")
    @AuditAction(value = "UPDATE_SPU", targetType = "spu")
    public ResponseEntity<byte[]> update(@PathVariable("id") Long id,
                                         HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/products/" + id, request, body);
    }

    @PutMapping("/products/{id}/skus")
    @RequirePermission("product:write")
    @AuditAction(value = "SAVE_SKUS", targetType = "spu")
    public ResponseEntity<byte[]> saveSkus(@PathVariable("id") Long id,
                                           HttpServletRequest request,
                                           @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/products/" + id + "/skus", request, body);
    }

    @DeleteMapping("/skus/{skuId}")
    @RequirePermission("product:write")
    @AuditAction(value = "DELETE_SKU", targetType = "sku")
    public ResponseEntity<byte[]> deleteSku(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        return proxy.forward("/admin/skus/" + skuId, request, null);
    }

    @PostMapping("/products/{id}/publish")
    @RequirePermission("product:write")
    @AuditAction(value = "PUBLISH_SPU", targetType = "spu")
    public ResponseEntity<byte[]> publish(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/products/" + id + "/publish", request, null);
    }

    @PostMapping("/products/{id}/offline")
    @RequirePermission("product:write")
    @AuditAction(value = "OFFLINE_SPU", targetType = "spu")
    public ResponseEntity<byte[]> offline(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/products/" + id + "/offline", request, null);
    }

    @PostMapping("/products/batch-status")
    @RequirePermission("product:write")
    @AuditAction(value = "BATCH_PRODUCT_STATUS", targetType = "spu")
    public ResponseEntity<byte[]> batchStatus(HttpServletRequest request,
                                              @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/products/batch-status", request, body);
    }

    @PutMapping("/skus/{skuId}/stock")
    @RequirePermission("product:write")
    @AuditAction(value = "ADJUST_STOCK", targetType = "sku")
    public ResponseEntity<byte[]> updateStock(@PathVariable("skuId") Long skuId,
                                              HttpServletRequest request,
                                              @RequestBody(required = false) byte[] body) {
        return proxy.forward("/admin/skus/" + skuId + "/stock", request, body);
    }
}
