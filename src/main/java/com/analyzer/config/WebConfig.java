package com.analyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for enabling CORS (Cross-Origin Resource Sharing).
 * This allows the frontend to call our API from any origin.
 * Static resources (HTML, CSS, JS) are automatically served from src/main/resources/static/
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow all origins to access our API
        // In production, you should restrict this to your actual domain
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
