package com.comonon.mall.order.config;

import com.comonon.mall.order.security.OrderUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    private final OrderUserInterceptor orderUserInterceptor;

    public OrderWebConfig(OrderUserInterceptor orderUserInterceptor) {
        this.orderUserInterceptor = orderUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 仅 C 端订单接口需要用户身份；/internal 与 /admin 不拦截
        registry.addInterceptor(orderUserInterceptor).addPathPatterns("/orders/**");
    }
}
