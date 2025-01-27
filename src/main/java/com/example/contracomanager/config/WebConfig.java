package com.example.contracomanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:4200",
                "https://workwave-0z0i.onrender.com",
                "https://contracomanager.onrender.com"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only handle requests that explicitly target static content
        registry.addResourceHandler("/static/**")
               .addResourceLocations("classpath:/static/")
               .setCachePeriod(3600)
               .resourceChain(false);
               
        // Exclude API paths from resource handling
        registry.setOrder(Integer.MAX_VALUE);
    }
} 