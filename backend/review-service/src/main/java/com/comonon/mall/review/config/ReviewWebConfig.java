package com.comonon.mall.review.config;

import com.comonon.mall.review.security.ReviewUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ReviewWebConfig implements WebMvcConfigurer {

    private final ReviewUserInterceptor reviewUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reviewUserInterceptor)
                .addPathPatterns("/reviews/**")
                .excludePathPatterns("/reviews/spu/**");
    }
}
