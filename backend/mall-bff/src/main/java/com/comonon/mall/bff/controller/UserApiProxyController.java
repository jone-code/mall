package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.proxy.UserApiProxy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 通用 user 路径透传：/api/user/** → user-service /**。
 * 禁止透传 /internal/**、/actuator/**，避免 C 端越权访问内部接口。
 */
@RestController
public class UserApiProxyController {

    private final UserApiProxy proxy;

    public UserApiProxyController(UserApiProxy proxy) {
        this.proxy = proxy;
    }

    @RequestMapping(value = "/api/user/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        // /api/user/foo -> /foo
        String fullPath = request.getRequestURI();
        String target = fullPath.startsWith("/api/user")
                ? fullPath.substring("/api/user".length())
                : fullPath;
        if (target.isEmpty()) {
            target = "/";
        }
        if (isBlockedTarget(target)) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":404,\"message\":\"Not Found\"}".getBytes(StandardCharsets.UTF_8));
        }
        return proxy.forward(target, request, body);
    }

    /** 内部/运维路径仅允许服务间直连，不可经 BFF 暴露给 C 端。 */
    private static boolean isBlockedTarget(String target) {
        if (target == null || target.isEmpty()) {
            return false;
        }
        String normalized = target.startsWith("/") ? target : "/" + target;
        return normalized.startsWith("/internal/")
                || normalized.equals("/internal")
                || normalized.startsWith("/actuator/")
                || normalized.equals("/actuator");
    }
}
