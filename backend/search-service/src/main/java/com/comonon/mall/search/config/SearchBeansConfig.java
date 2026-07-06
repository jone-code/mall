package com.comonon.mall.search.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public WebClient productServiceWebClient(SearchProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getProduct()).build();
    }
}
