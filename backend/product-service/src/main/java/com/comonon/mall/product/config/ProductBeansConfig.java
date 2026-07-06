package com.comonon.mall.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ProductProperties.class)
public class ProductBeansConfig {

    @Bean
    public WebClient searchServiceWebClient(ProductProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getSearch()).build();
    }

    @Bean
    public WebClient orderServiceWebClient(ProductProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getOrder()).build();
    }
}
