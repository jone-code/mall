package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.CartApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartAggregateController {

    private final CartApiProxy proxy;

    public CartAggregateController(CartApiProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/api/cart")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/cart", request, null);
    }

    @PostMapping("/api/cart/items")
    public ResponseEntity<byte[]> add(HttpServletRequest request,
                                      @RequestBody(required = false) byte[] body) {
        return proxy.forward("/cart/items", request, body);
    }

    @PutMapping("/api/cart/items/{skuId}")
    public ResponseEntity<byte[]> update(@PathVariable("skuId") Long skuId,
                                         HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/cart/items/" + skuId, request, body);
    }

    @PutMapping("/api/cart/select-all")
    public ResponseEntity<byte[]> selectAll(HttpServletRequest request,
                                            @RequestBody(required = false) byte[] body) {
        return proxy.forward("/cart/select-all", request, body);
    }

    @DeleteMapping("/api/cart/items/{skuId}")
    public ResponseEntity<byte[]> delete(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        return proxy.forward("/cart/items/" + skuId, request, null);
    }

    @DeleteMapping("/api/cart/invalid")
    public ResponseEntity<byte[]> deleteInvalid(HttpServletRequest request) {
        return proxy.forward("/cart/invalid", request, null);
    }

    @GetMapping("/api/cart/checkout-preview")
    public ResponseEntity<byte[]> checkoutPreview(HttpServletRequest request) {
        return proxy.forward("/cart/checkout-preview", request, null);
    }
}
