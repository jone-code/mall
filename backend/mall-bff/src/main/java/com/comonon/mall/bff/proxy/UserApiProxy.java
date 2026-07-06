package com.comonon.mall.bff.proxy;

import com.comonon.mall.bff.interceptor.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * UserApiProxy：把 BFF 的 /api/** 请求透传到 user-service。
 *
 * <p>透传策略：
 * <ul>
 *     <li>保留 Authorization、X-* 头</li>
 *     <li>注入 X-User-Id / X-Device-Type（拦截器赋值的 request attribute）</li>
 *     <li>原样转发 method / body / query string</li>
 * </ul>
 */
@Slf4j
@Component
public class UserApiProxy {

    private final WebClient userServiceWebClient;

    public UserApiProxy(@Qualifier("userServiceWebClient") WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    /**
     * 转发到 user-service。
     *
     * @param targetPath 下游路径，例如 {@code /me}
     * @param request    入站请求
     * @param body       原始请求体（GET 时为 null）
     * @return 上游响应
     */
    public ResponseEntity<byte[]> forward(String targetPath, HttpServletRequest request, byte[] body) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String targetUri = buildTargetPath(targetPath, request.getQueryString());

        WebClient.RequestBodySpec spec = userServiceWebClient
                .method(method)
                .uri(targetUri)
                .headers(headers -> copyHeaders(request, headers));

        Mono<ResponseEntity<byte[]>> mono;
        if (body != null && body.length > 0) {
            mono = spec.bodyValue(body).exchangeToMono(resp -> resp.toEntity(byte[].class));
        } else {
            mono = spec.exchangeToMono(resp -> resp.toEntity(byte[].class));
        }

        return ProxyResponseHelper.sanitize(mono.onErrorResume(ex -> {
            log.warn("Proxy to user-service failed: path={}, err={}", targetPath, ex.getMessage());
            return Mono.just(ResponseEntity
                    .status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"code\":502,\"message\":\"upstream error\"}")
                            .getBytes(StandardCharsets.UTF_8)));
        }).block());
    }

    /** 相对路径 + query，交给 WebClient 拼到 baseUrl（勿用 URI.create，否则会落到 localhost:80） */
    private String buildTargetPath(String path, String query) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (StringUtils.hasText(query)) {
            return normalized + "?" + query;
        }
        return normalized;
    }

    private void copyHeaders(HttpServletRequest request, HttpHeaders out) {
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            // 跳过 hop-by-hop / WebClient 自动管理的头
            if (name.equalsIgnoreCase(HttpHeaders.HOST)
                    || name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                    || name.equalsIgnoreCase(HttpHeaders.CONNECTION)
                    || name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                continue;
            }
            // 仅保留 Authorization 与 X-* 自定义头，其余按原样传
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                out.add(name, values.nextElement());
            }
        }

        // 拦截器解析后的身份信息一律覆写，防止下游被伪造头欺骗
        Object userId = request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        Object deviceType = request.getAttribute(AuthInterceptor.ATTR_DEVICE_TYPE);
        if (userId != null) {
            out.set(AuthInterceptor.ATTR_USER_ID, String.valueOf(userId));
        } else {
            out.remove(AuthInterceptor.ATTR_USER_ID);
        }
        if (deviceType != null) {
            out.set(AuthInterceptor.ATTR_DEVICE_TYPE, String.valueOf(deviceType));
        } else {
            out.remove(AuthInterceptor.ATTR_DEVICE_TYPE);
        }
    }
}
