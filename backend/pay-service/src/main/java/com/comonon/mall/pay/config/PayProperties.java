package com.comonon.mall.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class PayProperties {

    private Services services = new Services();

    @Data
    public static class Services {
        private String order = "http://localhost:8105";
    }
}
