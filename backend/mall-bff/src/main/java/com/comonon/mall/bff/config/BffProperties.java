package com.comonon.mall.bff.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * mall-bff 鉴权及下游服务配置。
 */
@Data
@ConfigurationProperties(prefix = "mall.bff")
public class BffProperties {

    private Auth auth = new Auth();
    private Services services = new Services();

    @Data
    public static class Auth {
        /** JWT 签名密钥，与 user-service 共享 */
        private String jwtSecret;
        /** 白名单（精确前缀匹配） */
        private List<String> whitelist = new ArrayList<>();
    }

    @Data
    public static class Services {
        /** user-service 基础地址 */
        private String user = "http://localhost:8101";
        /** product-service 基础地址 */
        private String product = "http://localhost:8103";
        /** cart-service 基础地址 */
        private String cart = "http://localhost:8104";
        /** order-service 基础地址 */
        private String order = "http://localhost:8105";
        /** pay-service 基础地址 */
        private String pay = "http://localhost:8106";
        /** search-service 基础地址 */
        private String search = "http://localhost:8108";
        /** review-service 基础地址 */
        private String review = "http://localhost:8109";
    }
}
