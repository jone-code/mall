package com.comonon.mall.admin.web;

import com.comonon.mall.admin.proxy.ReviewApiProxy;
import com.comonon.mall.admin.security.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminReviewProxyController {

    private final ReviewApiProxy proxy;

    @GetMapping("/reviews/stats")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> stats(HttpServletRequest request) {
        return proxy.forward("/admin/reviews/stats", request, null);
    }

    @GetMapping("/reviews")
    @RequirePermission("order:read")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/admin/reviews", request, null);
    }

    @PostMapping("/reviews/{id}/unhide")
    @RequirePermission("order:write")
    public ResponseEntity<byte[]> unhide(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/reviews/" + id + "/unhide", request, null);
    }

    @PostMapping("/reviews/{id}/hide")
    @RequirePermission("order:write")
    public ResponseEntity<byte[]> hide(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/admin/reviews/" + id + "/hide", request, null);
    }
}
