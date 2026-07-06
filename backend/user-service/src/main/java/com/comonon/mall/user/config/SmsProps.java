package com.comonon.mall.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "mall.sms")
public class SmsProps {
    private Duration codeTtl = Duration.ofMinutes(5);
    private Duration cooldown = Duration.ofSeconds(60);
    private int dailyLimit = 10;
    private Duration ipWindow = Duration.ofMinutes(5);
    private int ipLimit = 20;
    private int maxTries = 5;

    public Duration getCodeTtl() { return codeTtl; }
    public void setCodeTtl(Duration codeTtl) { this.codeTtl = codeTtl; }
    public Duration getCooldown() { return cooldown; }
    public void setCooldown(Duration cooldown) { this.cooldown = cooldown; }
    public int getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(int dailyLimit) { this.dailyLimit = dailyLimit; }
    public Duration getIpWindow() { return ipWindow; }
    public void setIpWindow(Duration ipWindow) { this.ipWindow = ipWindow; }
    public int getIpLimit() { return ipLimit; }
    public void setIpLimit(int ipLimit) { this.ipLimit = ipLimit; }
    public int getMaxTries() { return maxTries; }
    public void setMaxTries(int maxTries) { this.maxTries = maxTries; }
}
