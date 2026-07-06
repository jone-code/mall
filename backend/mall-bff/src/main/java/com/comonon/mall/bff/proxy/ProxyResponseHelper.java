package com.comonon.mall.bff.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * 清理下游响应中的 hop-by-hop 头，避免与 Servlet 容器重复 chunked 编码导致客户端解析失败。
 */
public final class ProxyResponseHelper {

    private ProxyResponseHelper() {}

    public static ResponseEntity<byte[]> sanitize(ResponseEntity<byte[]> upstream) {
        if (upstream == null) {
            return ResponseEntity.internalServerError().build();
        }
        HttpHeaders headers = new HttpHeaders();
        upstream.getHeaders().forEach((name, values) -> {
            if (isHopByHop(name)) {
                return;
            }
            values.forEach(v -> headers.add(name, v));
        });
        return ResponseEntity.status(upstream.getStatusCode())
                .headers(headers)
                .body(upstream.getBody());
    }

    private static boolean isHopByHop(String name) {
        if (name == null) {
            return true;
        }
        return switch (name.toLowerCase()) {
            case "transfer-encoding", "connection", "keep-alive", "proxy-authenticate",
                 "proxy-authorization", "te", "trailers", "upgrade", "content-length" -> true;
            default -> false;
        };
    }
}
