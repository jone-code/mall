package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.UserApiProxy;
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
public class AddressAggregateController {

    private final UserApiProxy proxy;

    public AddressAggregateController(UserApiProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/api/me/addresses")
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxy.forward("/me/addresses", request, null);
    }

    @PostMapping("/api/me/addresses")
    public ResponseEntity<byte[]> create(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/me/addresses", request, body);
    }

    @PutMapping("/api/me/addresses/{id}")
    public ResponseEntity<byte[]> update(@PathVariable("id") Long id,
                                       HttpServletRequest request,
                                       @RequestBody(required = false) byte[] body) {
        return proxy.forward("/me/addresses/" + id, request, body);
    }

    @PutMapping("/api/me/addresses/{id}/default")
    public ResponseEntity<byte[]> setDefault(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/me/addresses/" + id + "/default", request, null);
    }

    @DeleteMapping("/api/me/addresses/{id}")
    public ResponseEntity<byte[]> delete(@PathVariable("id") Long id, HttpServletRequest request) {
        return proxy.forward("/me/addresses/" + id, request, null);
    }
}
