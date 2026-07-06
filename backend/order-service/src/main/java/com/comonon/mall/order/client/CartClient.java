package com.comonon.mall.order.client;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CartClient {

    private final WebClient cartServiceWebClient;

    public CartClient(WebClient cartServiceWebClient) {
        this.cartServiceWebClient = cartServiceWebClient;
    }

    /** 取购物车勾选项 [{skuId, quantity}]。 */
    public List<long[]> selectedLines(Long userId) {
        JsonNode root;
        try {
            root = cartServiceWebClient.get().uri("/internal/cart/{userId}/checkout", userId)
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("cart selected call failed: {}", e.getMessage());
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "购物车服务不可用");
        }
        List<long[]> result = new ArrayList<>();
        if (root != null && root.path("code").asInt(-1) == 0) {
            JsonNode data = root.path("data");
            if (data.isArray()) {
                for (JsonNode node : data) {
                    long skuId = node.path("skuId").asLong();
                    long qty = node.path("quantity").asLong();
                    if (skuId > 0 && qty > 0) {
                        result.add(new long[]{skuId, qty});
                    }
                }
            }
        }
        return result;
    }

    /** 下单后移除已下单的购物车行（失败不影响订单）。 */
    public void removeLines(Long userId, List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        try {
            cartServiceWebClient.post().uri("/internal/cart/{userId}/remove", userId)
                    .bodyValue(Map.of("skuIds", skuIds))
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("cart remove call failed userId={}: {}", userId, e.getMessage());
        }
    }
}
