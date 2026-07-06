package com.comonon.mall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class OrderProperties {

    private Order order = new Order();
    private Services services = new Services();

    @Data
    public static class Order {
        private int timeoutSeconds = 1800;
    }

    @Data
    public static class Services {
        private String product = "http://localhost:8103";
        private String cart = "http://localhost:8104";
        private String user = "http://localhost:8101";
        private String pay = "http://localhost:8106";
    }
}
