package com.comonon.mall.search.client;

import com.comonon.mall.search.client.dto.SpuIndexDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ProductClient {

    private final WebClient productServiceWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductClient(WebClient productServiceWebClient) {
        this.productServiceWebClient = productServiceWebClient;
    }

    public SpuIndexDto fetchForIndex(Long spuId) {
        try {
            JsonNode root = productServiceWebClient.get()
                    .uri("/internal/search/spu/{id}", spuId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || root.path("data").isMissingNode() || root.path("data").isNull()) {
                return null;
            }
            return objectMapper.treeToValue(root.path("data"), SpuIndexDto.class);
        } catch (Exception e) {
            log.warn("fetch spu index doc failed id={}", spuId, e);
            return null;
        }
    }

    public List<SpuIndexDto> fetchOnSalePage(int page, int size) {
        try {
            JsonNode root = productServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/search/on-sale")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null || !root.path("data").path("list").isArray()) {
                return List.of();
            }
            List<SpuIndexDto> list = new ArrayList<>();
            for (JsonNode node : root.path("data").path("list")) {
                list.add(objectMapper.treeToValue(node, SpuIndexDto.class));
            }
            return list;
        } catch (Exception e) {
            log.warn("fetch on-sale page failed page={}", page, e);
            return List.of();
        }
    }

    public long fetchOnSaleTotal() {
        try {
            JsonNode root = productServiceWebClient.get()
                    .uri("/internal/search/on-sale?page=1&size=1")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null) {
                return 0;
            }
            return root.path("data").path("total").asLong(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
