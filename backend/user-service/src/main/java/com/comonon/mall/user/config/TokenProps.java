package com.comonon.mall.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "mall.token")
public class TokenProps {
    private Duration refreshTtl = Duration.ofDays(7);
    private Duration absoluteMax = Duration.ofDays(30);

    public Duration getRefreshTtl() { return refreshTtl; }
    public void setRefreshTtl(Duration refreshTtl) { this.refreshTtl = refreshTtl; }
    public Duration getAbsoluteMax() { return absoluteMax; }
    public void setAbsoluteMax(Duration absoluteMax) { this.absoluteMax = absoluteMax; }
}
