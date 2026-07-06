package com.comonon.mall.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ProductDetailEnricher {

    private final WebClient productServiceWebClient;
    private final WebClient orderServiceWebClient;
    private final ObjectMapper objectMapper;

    public ProductDetailEnricher(@Qualifier("productServiceWebClient") WebClient productServiceWebClient,
                                 @Qualifier("orderServiceWebClient") WebClient orderServiceWebClient,
                                 ObjectMapper objectMapper) {
        this.productServiceWebClient = productServiceWebClient;
        this.orderServiceWebClient = orderServiceWebClient;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<byte[]> productDetail(Long spuId, String query) {
        String uri = "/products/" + spuId + (query != null && !query.isBlank() ? "?" + query : "");
        byte[] body = productServiceWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
        if (body == null || body.length == 0) {
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":502,\"message\":\"upstream error\"}".getBytes(StandardCharsets.UTF_8));
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.path("code").asInt(-1) != 0) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
            }
            JsonNode data = root.path("data");
            String productType = data.path("productType").asText("");
            if ("VIRTUAL".equals(productType) || "SERVICE".equals(productType)) {
                long poolAvailable = fetchPoolAvailable(spuId, productType);
                if (data instanceof ObjectNode objectNode) {
                    objectNode.put("poolAvailable", poolAvailable);
                    for (JsonNode sku : objectNode.path("skus")) {
                        if (sku instanceof ObjectNode skuNode) {
                            int skuAvail = skuNode.path("available").asInt(0);
                            skuNode.put("available", (int) Math.min(skuAvail, poolAvailable));
                        }
                    }
                }
                body = objectMapper.writeValueAsBytes(root);
            }
        } catch (Exception e) {
            log.warn("enrich product detail failed spuId={}: {}", spuId, e.getMessage());
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private long fetchPoolAvailable(Long spuId, String productType) {
        try {
            JsonNode root = orderServiceWebClient.get()
                    .uri("/internal/pool/available?spuId={spuId}&productType={productType}", spuId, productType)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root != null && root.path("code").asInt(-1) == 0) {
                return root.path("data").path("available").asLong(0);
            }
        } catch (Exception e) {
            log.warn("fetch pool available failed spuId={}: {}", spuId, e.getMessage());
        }
        return 0;
    }
}
