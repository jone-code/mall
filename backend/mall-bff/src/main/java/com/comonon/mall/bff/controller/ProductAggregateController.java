package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.ProductApiProxy;
import com.comonon.mall.bff.service.ProductDetailEnricher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductAggregateController {

    private final ProductApiProxy proxy;
    private final ProductDetailEnricher productDetailEnricher;

    public ProductAggregateController(ProductApiProxy proxy, ProductDetailEnricher productDetailEnricher) {
        this.proxy = proxy;
        this.productDetailEnricher = productDetailEnricher;
    }

    @GetMapping("/api/home")
    public ResponseEntity<byte[]> home(HttpServletRequest request) {
        return proxy.forward("/home", request, null);
    }

    @GetMapping("/api/categories")
    public ResponseEntity<byte[]> categories(HttpServletRequest request) {
        return proxy.forward("/categories/tree", request, null);
    }

    @GetMapping("/api/products")
    public ResponseEntity<byte[]> products(HttpServletRequest request) {
        return proxy.forward("/products", request, null);
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<byte[]> productDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        return productDetailEnricher.productDetail(id, request.getQueryString());
    }
}
