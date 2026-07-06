package com.comonon.mall.admin.client;

import com.comonon.mall.admin.vo.MemberOrderBriefVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OrderQueryClient {

    private final WebClient orderServiceWebClient;
    private final ObjectMapper objectMapper;

    public OrderQueryClient(@Qualifier("orderServiceWebClient") WebClient orderServiceWebClient,
                            ObjectMapper objectMapper) {
        this.orderServiceWebClient = orderServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public List<MemberOrderBriefVO> recentOrders(Long userId, int size) {
        if (userId == null) {
            return Collections.emptyList();
        }
        JsonNode root;
        try {
            root = orderServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/admin/orders")
                            .queryParam("userId", userId)
                            .queryParam("page", 1)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.warn("fetch member orders failed userId={}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
        if (root == null || root.path("code").asInt(-1) != 0) {
            return Collections.emptyList();
        }
        JsonNode list = root.path("data").path("list");
        if (!list.isArray()) {
            return Collections.emptyList();
        }
        List<MemberOrderBriefVO> result = new ArrayList<>();
        for (JsonNode node : list) {
            MemberOrderBriefVO vo = new MemberOrderBriefVO();
            vo.setOrderNo(node.path("orderNo").asText(null));
            vo.setStatus(node.path("status").asText(null));
            if (node.hasNonNull("payAmount")) {
                vo.setPayAmount(new BigDecimal(node.path("payAmount").asText("0")));
            }
            vo.setCreatedAt(node.path("createdAt").asText(null));
            result.add(vo);
        }
        return result;
    }
}
