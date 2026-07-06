package com.comonon.mall.order.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OrderProperties.class)
public class OrderBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }

    @Bean
    public WebClient productServiceWebClient(OrderProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getProduct()).build();
    }

    @Bean
    public WebClient cartServiceWebClient(OrderProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getCart()).build();
    }

    @Bean
    public WebClient userServiceWebClient(OrderProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getUser()).build();
    }

    @Bean
    public WebClient payServiceWebClient(OrderProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getPay()).build();
    }
}
