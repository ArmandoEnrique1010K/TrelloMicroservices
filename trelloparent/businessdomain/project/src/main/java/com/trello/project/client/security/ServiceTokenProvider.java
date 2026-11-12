package com.trello.project.client.security;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.trello.project.client.dto.response.ServiceTokenResponse;
import com.trello.project.client.services.IdentityClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {
    private static final long TOKEN_RENEWAL_THRESHOLD_SECONDS = 30;

    private final IdentityClientService identityClientService;

    private String accessToken;
    private Instant expiresAt;

    public synchronized String getAccessToken() {

        if (isTokenValid()) {
            return accessToken;
        }

        log.info("Solicitando un nuevo JWT interno a Identity");

        ServiceTokenResponse response = identityClientService.generateInternalToken();

        if (response == null ||
                response.getAccessToken() == null ||
                response.getAccessToken().isBlank() ||
                response.getExpiresAt() == null) {

            throw new IllegalStateException(
                    "Identity devolvió una respuesta de JWT interno inválida");
        }

        this.accessToken = response.getAccessToken();
        this.expiresAt = response.getExpiresAt();

        log.info("JWT interno obtenido correctamente. Expira en: {}", expiresAt);

        return accessToken;
    }

    private boolean isTokenValid() {

        if (accessToken == null || expiresAt == null) {
            return false;
        }

        Instant renewalTime = Instant.now()
                .plusSeconds(TOKEN_RENEWAL_THRESHOLD_SECONDS);

        return renewalTime.isBefore(expiresAt);
    }

}
