package com.comonon.mall.review.config;

import com.comonon.mall.common.exception.GlobalExceptionHandler;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@ConfigurationProperties(prefix = "mall")
class ReviewProperties {
    private Services services = new Services();

    @Data
    static class Services {
        private String order = "http://localhost:8105";
        private String user = "http://localhost:8101";
    }
}

@Configuration
@EnableConfigurationProperties(ReviewProperties.class)
public class ReviewBeansConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }

    @Bean
    public WebClient orderServiceWebClient(ReviewProperties props) {
        return WebClient.builder().baseUrl(props.getServices().getOrder()).build();
    }
}
