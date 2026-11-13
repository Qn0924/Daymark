package com.nan.daymarkbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${daymark.upload.dir}")
    private String uploadDir;

    @Value("${daymark.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /api/static/** 映射到 本地 uploads 目录
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}