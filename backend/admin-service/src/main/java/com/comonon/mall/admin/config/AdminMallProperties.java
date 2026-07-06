package com.comonon.mall.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class AdminMallProperties {
    private Services services = new Services();

    @Data
    public static class Services {
        private String product = "http://localhost:8103";
        private String order = "http://localhost:8105";
        private String review = "http://localhost:8109";
    }
}
