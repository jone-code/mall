package com.comonon.mall.cart.config;

import com.comonon.mall.cart.security.CartUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CartWebConfig implements WebMvcConfigurer {

    private final CartUserInterceptor cartUserInterceptor;

    public CartWebConfig(CartUserInterceptor cartUserInterceptor) {
        this.cartUserInterceptor = cartUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartUserInterceptor).addPathPatterns("/cart/**");
    }
}
