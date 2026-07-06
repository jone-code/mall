package com.comonon.mall.product.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }
}
