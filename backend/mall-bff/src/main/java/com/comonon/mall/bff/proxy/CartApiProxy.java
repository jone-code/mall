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

@Slf4j
@Component
public class CartApiProxy {

    private final WebClient cartServiceWebClient;

    public CartApiProxy(@Qualifier("cartServiceWebClient") WebClient cartServiceWebClient) {
        this.cartServiceWebClient = cartServiceWebClient;
    }

    public ResponseEntity<byte[]> forward(String targetPath, HttpServletRequest request, byte[] body) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String targetUri = buildTargetPath(targetPath, request.getQueryString());

        WebClient.RequestBodySpec spec = cartServiceWebClient.method(method).uri(targetUri)
                .headers(headers -> copyHeaders(request, headers));

        Mono<ResponseEntity<byte[]>> mono;
        if (body != null && body.length > 0) {
            mono = spec.bodyValue(body).exchangeToMono(resp -> resp.toEntity(byte[].class));
        } else {
            mono = spec.exchangeToMono(resp -> resp.toEntity(byte[].class));
        }

        return ProxyResponseHelper.sanitize(mono.onErrorResume(ex -> {
            log.warn("Proxy to cart-service failed: path={}, err={}", targetPath, ex.getMessage());
            return Mono.just(ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":502,\"message\":\"upstream error\"}"
                            .getBytes(StandardCharsets.UTF_8)));
        }).block());
    }

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
            if (name.equalsIgnoreCase(HttpHeaders.HOST)
                    || name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                    || name.equalsIgnoreCase(HttpHeaders.CONNECTION)
                    || name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                out.add(name, values.nextElement());
            }
        }
        Object userId = request.getAttribute(AuthInterceptor.ATTR_USER_ID);
        if (userId != null) {
            out.set(AuthInterceptor.ATTR_USER_ID, String.valueOf(userId));
        } else {
            // 防止客户端伪造 X-User-Id 头越权访问他人购物车
            out.remove(AuthInterceptor.ATTR_USER_ID);
        }
    }
}
