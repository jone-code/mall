package com.comonon.mall.product.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class SearchIndexClient {

    private final WebClient searchServiceWebClient;

    public SearchIndexClient(WebClient searchServiceWebClient) {
        this.searchServiceWebClient = searchServiceWebClient;
    }

    public void indexSpu(Long spuId) {
        try {
            searchServiceWebClient.put()
                    .uri("/internal/index/spu/{id}", spuId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.warn("notify search index failed spuId={}", spuId, e);
        }
    }

    public void deleteSpu(Long spuId) {
        try {
            searchServiceWebClient.delete()
                    .uri("/internal/index/spu/{id}", spuId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.warn("notify search delete failed spuId={}", spuId, e);
        }
    }
}
