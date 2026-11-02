package com.personal.marketnote.product.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ProductExecutorConfig {
    @Bean(name = "productImageExecutor")
    public Executor productImageExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
