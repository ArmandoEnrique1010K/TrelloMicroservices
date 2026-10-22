package com.trello.workflow.client.config;

import java.lang.reflect.Method;

import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Target;

@Configuration
public class FeignCircuitBreakerConfig {
    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (String feignClientName, Target<?> target, Method method) -> feignClientName;
    }
}
