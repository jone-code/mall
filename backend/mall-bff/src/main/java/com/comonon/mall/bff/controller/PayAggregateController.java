package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.PayApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayAggregateController {

    private final PayApiProxy proxy;

    public PayAggregateController(PayApiProxy proxy) {
        this.proxy = proxy;
    }

    @PostMapping("/api/orders/{orderNo}/pay")
    public ResponseEntity<byte[]> create(@PathVariable("orderNo") String orderNo, HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/orders/" + orderNo + "/pay", request, body);
    }

    @PostMapping("/api/pay/{payNo}/confirm")
    public ResponseEntity<byte[]> confirm(@PathVariable("payNo") String payNo, HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        return proxy.forward("/pay/" + payNo + "/confirm", request, body);
    }

    @GetMapping("/api/pay/{payNo}")
    public ResponseEntity<byte[]> query(@PathVariable("payNo") String payNo, HttpServletRequest request) {
        return proxy.forward("/pay/" + payNo, request, null);
    }
}
