package com.trello.project.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    // @Bean
    // public RequestInterceptor feignJwtInterceptor() {
    // return requestTemplate -> {

    // ServletRequestAttributes attributes = (ServletRequestAttributes)
    // RequestContextHolder
    // .getRequestAttributes();

    // // System.out.println("ATTRIBUTES: " + attributes);

    // if (attributes == null) {
    // System.out.println("No existe request HTTP actual");
    // return;
    // }

    // HttpServletRequest request = attributes.getRequest();

    // String authorization = request.getHeader("Authorization");

    // // System.out.println("AUTHORIZATION EN FEIGN: " + authorization);

    // if (authorization != null && !authorization.isBlank()) {
    // requestTemplate.header("Authorization", authorization);

    // // System.out.println(
    // // "AUTHORIZATION AGREGADO A FEIGN: " + authorization);
    // }
    // };

    // }

    // private final ServiceTokenProvider serviceTokenProvider;

    @Bean
    public RequestInterceptor feignJwtInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            if (attributes != null) {
                String authorization = attributes.getRequest()
                        .getHeader("Authorization");

                if (authorization != null && !authorization.isBlank()) {
                    requestTemplate.header("Authorization", authorization);
                    return;
                }
            }

            // Autenticación del microservicio Project.
            // String serviceToken = serviceTokenProvider.getToken();

            // requestTemplate.header(
            // "Authorization",
            // "Bearer " + serviceToken);
        };
    }

}