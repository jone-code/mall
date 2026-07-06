package com.comonon.mall.product.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 查询 order-service 订单状态，供库存过期扫描等内部逻辑使用。
 */
@Slf4j
@Component
public class OrderClient {

    private final WebClient orderServiceWebClient;

    public OrderClient(WebClient orderServiceWebClient) {
        this.orderServiceWebClient = orderServiceWebClient;
    }

    /** 订单不存在或服务不可用时返回 null，调用方应保守跳过释放。 */
    public String getStatus(String orderNo) {
        try {
            JsonNode root = orderServiceWebClient.get()
                    .uri("/internal/orders/{orderNo}/status", orderNo)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("code").asInt(-1) != 0) {
                return null;
            }
            JsonNode data = root.path("data");
            return data.isMissingNode() || data.isNull() ? null : data.asText();
        } catch (Exception e) {
            log.warn("order status query failed orderNo={}: {}", orderNo, e.getMessage());
            return null;
        }
    }
}
