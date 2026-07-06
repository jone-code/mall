package com.comonon.mall.pay.client;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.pay.client.dto.OrderInfoDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class OrderClient {

    private final WebClient orderServiceWebClient;
    private final ObjectMapper objectMapper;

    public OrderClient(WebClient orderServiceWebClient, ObjectMapper objectMapper) {
        this.orderServiceWebClient = orderServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public OrderInfoDto getOrder(String orderNo) {
        JsonNode root;
        try {
            root = orderServiceWebClient.get().uri("/internal/orders/{orderNo}", orderNo)
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("order get failed: {}", e.getMessage());
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "订单服务不可用");
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        try {
            return objectMapper.treeToValue(root.path("data"), OrderInfoDto.class);
        } catch (Exception e) {
            log.warn("order parse failed orderNo={}: {}", orderNo, e.getMessage());
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
    }

    public void notifyPaid(String orderNo) {
        JsonNode root;
        try {
            root = orderServiceWebClient.post().uri("/internal/orders/{orderNo}/paid", orderNo)
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (Exception e) {
            log.warn("order notifyPaid failed: {}", e.getMessage());
            throw BizException.of(ErrorCode.INTERNAL_ERROR, "订单服务不可用");
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            String msg = root == null ? "订单回填失败" : root.path("message").asText("订单回填失败");
            throw BizException.of(ErrorCode.INTERNAL_ERROR, msg);
        }
    }
}
