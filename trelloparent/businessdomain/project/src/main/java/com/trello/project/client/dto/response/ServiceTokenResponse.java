package com.trello.project.client.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ServiceTokenResponse", description = "Representa el JWT interno para las peticiones internas")
public class ServiceTokenResponse {
    @Schema(name = "accessToken", example = "eyJ...", description = "Token de acceso JWT")
    private String accessToken;

    @Schema(name = "expiresAt", example = "2025-12-31T23:59:59Z", description = "Fecha y hora UTC de expiración del token en formato ISO-8601 (Instant)")
    private Instant expiresAt;
}
