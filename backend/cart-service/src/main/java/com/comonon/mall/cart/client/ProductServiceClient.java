package com.comonon.mall.cart.client;

import com.comonon.mall.cart.client.dto.SkuSnapshotClientDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient productServiceWebClient;
    private final ObjectMapper objectMapper;

    public Map<Long, SkuSnapshotClientDto> fetchSnapshots(List<Long> skuIds) {
        Map<Long, SkuSnapshotClientDto> map = new HashMap<>();
        if (skuIds == null || skuIds.isEmpty()) {
            return map;
        }
        try {
            JsonNode root = productServiceWebClient.post()
                    .uri("/internal/sku/snapshot")
                    .bodyValue(Map.of("skuIds", skuIds))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("code").asInt(-1) != 0) {
                log.warn("product snapshot failed: {}", root);
                return map;
            }
            JsonNode items = root.path("data").path("items");
            if (!items.isArray()) {
                return map;
            }
            for (JsonNode node : items) {
                SkuSnapshotClientDto dto = objectMapper.treeToValue(node, SkuSnapshotClientDto.class);
                if (dto.getSkuId() != null) {
                    map.put(dto.getSkuId(), dto);
                }
            }
        } catch (Exception e) {
            log.warn("product snapshot error: {}", e.getMessage());
        }
        return map;
    }
}
