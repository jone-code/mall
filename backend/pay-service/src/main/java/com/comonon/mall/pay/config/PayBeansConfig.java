package com.comonon.mall.pay.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(PayProperties.class)
public class PayBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }

    @Bean
    public WebClient orderServiceWebClient(PayProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getOrder()).build();
    }
}
