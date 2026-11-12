package com.trello.project.client.config;

import org.springframework.context.annotation.Bean;

import com.trello.project.client.security.ServiceTokenProvider;

import feign.RequestInterceptor;

public class WorkflowFeignConfig {

    @Bean
    public RequestInterceptor workflowTokenInterceptor(
            ServiceTokenProvider serviceTokenProvider) {

        return requestTemplate -> {
            String token = serviceTokenProvider.getAccessToken();

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + token);
        };
    }
}
