package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.ReviewApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewAggregateController {

    private final ReviewApiProxy proxy;

    public ReviewAggregateController(ReviewApiProxy proxy) {
        this.proxy = proxy;
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<byte[]> create(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/reviews", request, body);
    }

    @GetMapping("/api/reviews/me")
    public ResponseEntity<byte[]> mine(HttpServletRequest request) {
        return proxy.forward("/reviews/me", request, null);
    }

    @GetMapping("/api/reviews/order/{orderNo}")
    public ResponseEntity<byte[]> byOrder(@PathVariable("orderNo") String orderNo,
                                          HttpServletRequest request) {
        return proxy.forward("/reviews/order/" + orderNo, request, null);
    }

    @GetMapping("/api/reviews/spu/{spuId}")
    public ResponseEntity<byte[]> bySpu(@PathVariable("spuId") Long spuId,
                                        HttpServletRequest request) {
        return proxy.forward("/reviews/spu/" + spuId, request, null);
    }

    @GetMapping("/api/reviews/spu/{spuId}/summary")
    public ResponseEntity<byte[]> summary(@PathVariable("spuId") Long spuId,
                                          HttpServletRequest request) {
        return proxy.forward("/reviews/spu/" + spuId + "/summary", request, null);
    }
}
