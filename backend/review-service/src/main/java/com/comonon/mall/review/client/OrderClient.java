package com.comonon.mall.review.client;

import com.comonon.mall.review.client.dto.OrderBriefDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class OrderClient {

    private final WebClient orderServiceWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderClient(WebClient orderServiceWebClient) {
        this.orderServiceWebClient = orderServiceWebClient;
    }

    public OrderBriefDto getOrder(String orderNo) {
        try {
            JsonNode root = orderServiceWebClient.get()
                    .uri("/internal/orders/{orderNo}", orderNo)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("data").isMissingNode()) {
                return null;
            }
            return objectMapper.treeToValue(root.path("data"), OrderBriefDto.class);
        } catch (Exception e) {
            log.warn("fetch order failed orderNo={}", orderNo, e);
            return null;
        }
    }
}
