package com.trello.identity.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Identity API").version("1.0.0"))

                // Configuración relacionada al esquema Bearer para pasar el JWT
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
        // Habilita el icono de candado en todos los endpoints - todos los endpoints
        // requieren autenticación
        // .addSecurityItem(
        // new SecurityRequirement()
        // .addList("bearerAuth"))
        ;
    }

}
