package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.SearchApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchAggregateController {

    private final SearchApiProxy proxy;

    public SearchAggregateController(SearchApiProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/api/search/products")
    public ResponseEntity<byte[]> searchProducts(HttpServletRequest request) {
        return proxy.forward("/search/products", request, null);
    }
}
