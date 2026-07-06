package com.comonon.mall.pay.config;

import com.comonon.mall.pay.security.PayUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PayWebConfig implements WebMvcConfigurer {

    private final PayUserInterceptor payUserInterceptor;

    public PayWebConfig(PayUserInterceptor payUserInterceptor) {
        this.payUserInterceptor = payUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(payUserInterceptor)
                .addPathPatterns("/pay/**", "/orders/**");
    }
}
