package com.trello.identity.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AuthenticationResponse", description = "Represents the result of the authentication")
public class AuthenticationResponse {

    @Schema(name = "confirmed", example = "true", description = "Indicates whether the user's account is confirmed")
    private boolean confirmed;

    @Schema(name = "accessToken", example = "eyJ...", description = "JWT access token for the current session")
    private String accessToken;

    // TODO: Pendiente investigar este campo para refrescar el token de
    // autenticación
    @Schema(name = "refreshToken", example = "eyJ...", description = "JWT refresh token used to obtain a new access token")
    private String refreshToken;

    @Schema(name = "expiresIn", example = "900", description = "Access token expiration time in seconds")
    private long expiresIn;
}