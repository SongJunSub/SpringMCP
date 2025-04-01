package com.example.springmcp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // favicon.ico 요청 처리
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
                
        // 기본 정적 리소스 처리
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
