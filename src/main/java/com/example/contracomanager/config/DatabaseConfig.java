package com.example.contracomanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.example.contracomanager.model")
@Slf4j
public class DatabaseConfig {
    
    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/workwave}")
    private String url;
    
    @Value("${spring.datasource.username:postgres}")
    private String username;
    
    @Value("${spring.datasource.password:admin}")
    private String password;
    
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring DataSource with URL: {}", url);
        try {
            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.postgresql.Driver");
            dataSourceBuilder.url(url);
            dataSourceBuilder.username(username);
            dataSourceBuilder.password(password);
            return dataSourceBuilder.build();
        } catch (Exception e) {
            log.error("Failed to create DataSource: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.info("Configuring EntityManagerFactory");
        try {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("com.example.contracomanager.model");
            
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(true);
            vendorAdapter.setShowSql(true);
            em.setJpaVendorAdapter(vendorAdapter);
            
            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "update");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
            em.setJpaProperties(properties);
            
            return em;
        } catch (Exception e) {
            log.error("Failed to create EntityManagerFactory: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        log.info("Configuring TransactionManager");
        try {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
            return transactionManager;
        } catch (Exception e) {
            log.error("Failed to create TransactionManager: {}", e.getMessage(), e);
            throw e;
        }
    }
} 