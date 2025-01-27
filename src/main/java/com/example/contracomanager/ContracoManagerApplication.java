package com.example.contracomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.contracomanager")
@EnableJpaRepositories(
    basePackages = "com.example.contracomanager.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EntityScan("com.example.contracomanager.model")
@EnableRetry
public class ContracoManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContracoManagerApplication.class, args);
    }

}
