package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.OrderApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderAggregateController {

    private final OrderApiProxy proxy;

    public OrderAggregateController(OrderApiProxy proxy) {
        this.proxy = proxy;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<byte[]> create(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/orders", request, body);
    }

    @GetMapping("/api/orders")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/orders", request, null);
    }

    @GetMapping("/api/orders/{orderNo}")
    public ResponseEntity<byte[]> detail(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        return proxy.forward("/orders/" + orderNo, request, null);
    }

    @PostMapping("/api/orders/{orderNo}/cancel")
    public ResponseEntity<byte[]> cancel(@PathVariable("orderNo") String orderNo, HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/orders/" + orderNo + "/cancel", request, body);
    }

    @PostMapping("/api/orders/{orderNo}/confirm")
    public ResponseEntity<byte[]> confirm(@PathVariable("orderNo") String orderNo, HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        return proxy.forward("/orders/" + orderNo + "/confirm", request, body);
    }

    @PostMapping("/api/orders/{orderNo}/refund")
    public ResponseEntity<byte[]> refund(@PathVariable("orderNo") String orderNo, HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/orders/" + orderNo + "/refund", request, body);
    }

    @GetMapping("/api/orders/{orderNo}/logistics")
    public ResponseEntity<byte[]> logistics(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        return proxy.forward("/orders/" + orderNo + "/logistics", request, null);
    }
}
