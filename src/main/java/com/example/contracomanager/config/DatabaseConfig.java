package com.example.contracomanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    @Value("${SPRING_DATASOURCE_URL}")
    private String url;
    
    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String username;
    
    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String password;
    
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder
            .create()
            .url(url)
            .username(username)
            .password(password)
            .driverClassName("org.postgresql.Driver")
            .build();
    }
} 