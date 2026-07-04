package com.droplink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static assets (CSS, JS)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        // Serve favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");

        // Serve index.html
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/");
    }
}
