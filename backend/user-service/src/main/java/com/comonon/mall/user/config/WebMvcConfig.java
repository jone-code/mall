package com.comonon.mall.user.config;

import com.comonon.mall.user.web.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    static final List<String> EXCLUDE = List.of(
            "/sms/send",
            "/login/sms",
            "/oauth/wechat",
            "/token/refresh",
            "/internal/**",
            "/error",
            "/actuator/**"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE);
    }
}
