package com.droplink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Ensure API paths are not caught by static resource handlers
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only serve static resources for non-API paths
        // API paths (/api/**) will be handled by controllers
        registry.addResourceHandler("/assets/**", "/favicon.ico", "/index.html")
                .addResourceLocations("classpath:/static/", "classpath:/static/assets/");
    }
}
