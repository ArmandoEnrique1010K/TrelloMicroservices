package com.trello.identity.auth.resend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {
    // API Key utilizada para autenticar las solicitudes con Resend.
    @Value("${resend.api.key}")
    private String apiKey;

    // Registra el cliente de Resend como un bean de Spring
    // para permitir su inyección por constructor en otros componentes.
    @Bean
    public Resend resendClient() {
        return new Resend(apiKey);
    }
}
