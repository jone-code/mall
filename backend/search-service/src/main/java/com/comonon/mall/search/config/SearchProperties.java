package com.comonon.mall.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall")
public class SearchProperties {
    private Search search = new Search();
    private Services services = new Services();

    @Data
    public static class Search {
        private String indexName = "mall_products";
    }

    @Data
    public static class Services {
        private String product = "http://localhost:8103";
    }
}
