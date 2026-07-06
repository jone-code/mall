package com.comonon.mall.admin.config;

import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.sms.SmsGateway;
import com.comonon.mall.common.sms.SmsGatewayStub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AdminMallProperties.class)
public class BeanConfig {

    @Bean
    public JwtUtil adminJwtUtil(@Value("${mall.jwt.admin-secret}") String secret,
                                @Value("${mall.jwt.issuer:comonon-admin}") String issuer) {
        return new JwtUtil(secret, issuer);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(
            @Value("${mall.admin.password.bcrypt-cost:12}") int cost) {
        return new BCryptPasswordEncoder(cost);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public SmsGateway smsGateway() {
        return new SmsGatewayStub();
    }

    @Bean
    public WebClient productServiceWebClient(AdminMallProperties properties) {
        String baseUrl = properties.getServices().getProduct();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8103";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient orderServiceWebClient(AdminMallProperties properties) {
        String baseUrl = properties.getServices().getOrder();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8105";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient reviewServiceWebClient(AdminMallProperties properties) {
        String baseUrl = properties.getServices().getReview();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "http://localhost:8109";
        }
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
