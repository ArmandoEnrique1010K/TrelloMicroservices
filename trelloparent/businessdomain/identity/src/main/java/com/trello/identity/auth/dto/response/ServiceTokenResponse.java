package com.trello.identity.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ServiceTokenResponse", description = "Representa el JWT interno para las peticiones internas")
public class ServiceTokenResponse {
    @Schema(name = "accessToken", example = "eyJ...", description = "Token de acceso JWT")
    private String accessToken;
}
