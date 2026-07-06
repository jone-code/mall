package com.comonon.mall.bff.config;

import com.comonon.mall.bff.interceptor.AuthInterceptor;
import com.comonon.mall.bff.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册全局鉴权拦截器，作用范围 /api/**；静态文件 /files/** 公开访问。
 */
@Configuration
@RequiredArgsConstructor
public class BffWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LocalFileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + fileStorageService.resolveRoot() + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");
    }
}
