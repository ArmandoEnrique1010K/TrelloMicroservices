package com.trello.project.client.config;

import java.lang.reflect.Method;

import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Target;

// En Spring Cloud 2025.1.2, el nombre de los circuit breaker se generan automaticamente
// Ve a http://localhost:8082/business/v1/actuator/circuitbreakers para ver los nombres generados, 
// el nombre se puede generar luego de hacer una petición exitosa en donde se llama a un microservicio
@Configuration
public class FeignCircuitBreakerConfig {
    /**
     * Personaliza el nombre de los Circuit Breakers utilizados
     * por los clientes OpenFeign.
     *
     * OpenFeign genera un Circuit Breaker por cada método del
     * cliente Feign. El nombre generado por defecto puede ser
     * diferente al nombre definido en @FeignClient.
     *
     */
    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {

        // En este caso se utiliza el siguiente formato:
        //
        // <nombre definido en @FeignClient>_<nombre del método>
        //
        // Por ejemplo:
        //
        // @FeignClient(name = "businessdomain-identity-user-query")
        // List<UserResponse> findUsersByIds(...)
        //
        // genera:
        //
        // businessdomain-identity-user-query_findUsersByIds
        //
        // Este nombre también se utiliza como identificador de la
        // instancia de Circuit Breaker configurada en
        // application.properties.

        // return (String feignClientName, Target<?> target, Method method) ->
        // feignClientName + "_" + method.getName();

        // Una forma más limpia es utilizar solamente el nombre del feignClient
        // Ahorra varias lineas de codigo en application.properties para no definir las
        // mismas propiedades por cada método definido en el FeignClient

        // genera:

        // businessdomain-identity-user-query
        return (String feignClientName, Target<?> target, Method method) -> feignClientName;
    }
}
