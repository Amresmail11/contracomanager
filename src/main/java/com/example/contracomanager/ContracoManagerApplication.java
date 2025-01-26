package com.example.contracomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.contracomanager")
@EnableRetry
public class ContracoManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContracoManagerApplication.class, args);
    }

}
