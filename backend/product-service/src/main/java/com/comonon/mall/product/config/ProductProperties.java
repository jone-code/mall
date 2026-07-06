package com.comonon.mall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class ProductProperties {
    private Services services = new Services();

    @Data
    public static class Services {
        private String search = "http://localhost:8108";
        private String order = "http://localhost:8105";
    }
}
