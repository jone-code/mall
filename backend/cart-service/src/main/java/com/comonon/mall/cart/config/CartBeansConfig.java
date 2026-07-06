package com.comonon.mall.cart.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(CartProperties.class)
public class CartBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }

    @Bean
    public WebClient productServiceWebClient(CartProperties properties) {
        String baseUrl = properties.getServices().getProduct();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8103";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
