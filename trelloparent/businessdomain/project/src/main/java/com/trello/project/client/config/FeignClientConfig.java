package com.trello.project.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

public class FeignClientConfig {

    @Bean
    public RequestInterceptor feignJwtInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            System.out.println("ATTRIBUTES: " + attributes);

            if (attributes == null) {
                System.out.println("No existe request HTTP actual");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader("Authorization");

            System.out.println("AUTHORIZATION EN FEIGN: " + authorization);

            if (authorization != null && !authorization.isBlank()) {
                requestTemplate.header("Authorization", authorization);

                System.out.println(
                        "AUTHORIZATION AGREGADO A FEIGN: " + authorization);
            }
        };
    }
}