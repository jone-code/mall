package com.comonon.mall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class CartProperties {
    private Cart cart = new Cart();
    private Services services = new Services();

    @Data
    public static class Cart {
        private int maxLines = 50;
        private int maxQuantityPerLine = 99;
    }

    @Data
    public static class Services {
        private String product = "http://localhost:8103";
    }
}
