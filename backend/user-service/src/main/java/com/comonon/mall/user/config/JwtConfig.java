package com.comonon.mall.user.config;

import com.comonon.mall.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtProps jwtProps() {
        return new JwtProps();
    }

    @Bean
    public JwtUtil jwtUtil(@Value("${mall.jwt.secret}") String secret,
                           @Value("${mall.jwt.access-ttl:2h}") Duration accessTtl) {
        return new JwtUtil(secret, accessTtl);
    }

    @ConfigurationProperties("mall.jwt")
    public static class JwtProps {
        private String secret;
        private Duration accessTtl = Duration.ofHours(2);
        private Duration refreshTtl = Duration.ofDays(7);

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public Duration getAccessTtl() { return accessTtl; }
        public void setAccessTtl(Duration accessTtl) { this.accessTtl = accessTtl; }
        public Duration getRefreshTtl() { return refreshTtl; }
        public void setRefreshTtl(Duration refreshTtl) { this.refreshTtl = refreshTtl; }
    }
}
