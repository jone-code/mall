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

/**
 * 6 个最小聚合路由，全部透传给 user-service：
 * <ul>
 *     <li>GET  /api/me</li>
 *     <li>PUT  /api/me</li>
 *     <li>POST /api/sms/send</li>
 *     <li>POST /api/login/sms</li>
 *     <li>POST /api/oauth/wechat</li>
 *     <li>POST /api/token/refresh</li>
 *     <li>POST /api/logout</li>
 * </ul>
 */
@RestController
public class AuthAggregateController {

    private final UserApiProxy proxy;

    public AuthAggregateController(UserApiProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/api/me")
    public ResponseEntity<byte[]> me(HttpServletRequest request) {
        return proxy.forward("/me", request, null);
    }

    @PutMapping("/api/me")
    public ResponseEntity<byte[]> updateMe(HttpServletRequest request,
                                           @RequestBody(required = false) byte[] body) {
        return proxy.forward("/me", request, body);
    }

    @PutMapping("/api/me/phone")
    public ResponseEntity<byte[]> changePhone(HttpServletRequest request,
                                              @RequestBody(required = false) byte[] body) {
        return proxy.forward("/me/phone", request, body);
    }

    @PostMapping("/api/me/avatar")
    public ResponseEntity<byte[]> mockAvatar(HttpServletRequest request,
                                             @RequestBody(required = false) byte[] body) {
        return proxy.forward("/me/avatar", request, body);
    }

    @PostMapping("/api/sms/send")
    public ResponseEntity<byte[]> smsSend(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        return proxy.forward("/sms/send", request, body);
    }

    @PostMapping("/api/login/sms")
    public ResponseEntity<byte[]> loginSms(HttpServletRequest request,
                                           @RequestBody(required = false) byte[] body) {
        return proxy.forward("/login/sms", request, body);
    }

    @PostMapping("/api/oauth/wechat")
    public ResponseEntity<byte[]> oauthWechat(HttpServletRequest request,
                                              @RequestBody(required = false) byte[] body) {
        return proxy.forward("/oauth/wechat", request, body);
    }

    @PostMapping("/api/token/refresh")
    public ResponseEntity<byte[]> refresh(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        return proxy.forward("/token/refresh", request, body);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<byte[]> logout(HttpServletRequest request,
                                         @RequestBody(required = false) byte[] body) {
        return proxy.forward("/logout", request, body);
    }

    @GetMapping("/api/me/sessions")
    public ResponseEntity<byte[]> mySessions(HttpServletRequest request) {
        return proxy.forward("/me/sessions", request, null);
    }

    @DeleteMapping("/api/me/sessions/{sid}")
    public ResponseEntity<byte[]> killSession(@PathVariable("sid") String sid,
                                              HttpServletRequest request) {
        return proxy.forward("/me/sessions/" + sid, request, null);
    }
}
