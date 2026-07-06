package com.comonon.mall.admin.config;

import com.comonon.mall.admin.security.AdminAuthInterceptor;
import com.comonon.mall.admin.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final LocalFileStorageService fileStorageService;

    /** 公开接口（不走鉴权拦截器）。 */
    public static final List<String> PUBLIC_PATHS = List.of(
            "/admin/captcha",
            "/admin/login/password",
            "/admin/login/verify",
            "/admin/token/refresh",
            "/files/**",
            "/error"
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/admin/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + fileStorageService.resolveRoot() + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }
}
