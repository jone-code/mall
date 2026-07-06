package com.comonon.mall.bff.config;

import com.comonon.mall.common.security.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * 基础 Bean：JwtUtil 与下游 WebClient。
 */
@Configuration
@EnableConfigurationProperties(BffProperties.class)
public class BffBeansConfig {

    /**
     * 与 user-service 共享同一签名密钥；BFF 仅做验签，不签发，access TTL 仅用于占位。
     */
    @Bean
    public JwtUtil jwtUtil(BffProperties properties) {
        return new JwtUtil(properties.getAuth().getJwtSecret(), Duration.ofHours(2));
    }

    @Bean
    public WebClient userServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getUser();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8101";
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient productServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getProduct();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8103";
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient cartServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getCart();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8104";
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient orderServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getOrder();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8105";
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient payServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getPay();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8106";
        }
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public WebClient searchServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getSearch();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8108";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient reviewServiceWebClient(BffProperties properties) {
        String baseUrl = properties.getServices().getReview();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8109";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
