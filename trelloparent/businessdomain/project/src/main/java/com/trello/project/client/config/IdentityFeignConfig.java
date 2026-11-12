package com.trello.project.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;

public class IdentityFeignConfig {

    @Value("${services.clients.project.username}")
    private String username;

    @Value("${services.clients.project.password}")
    private String password;

    @Bean
    public RequestInterceptor identityBasicAuthInterceptor() {
        return new BasicAuthRequestInterceptor(username, password);
    }

    @Bean
    public ErrorDecoder identityErrorDecoder() {
        return new ErrorDecoder.Default();
    }
}