package com.trello.identity.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AuthenticationResponse", description = "Información resultante del proceso de autenticación")
public class AuthenticationResponse {

    @Schema(name = "confirmed", example = "true", description = "Indica el estado de confirmación del usuario")
    private boolean confirmed;

    @Schema(name = "accessToken", example = "eyJ...", description = "Token de acceso JWT para la sesión actual")
    private String accessToken;

    // TODO: Pendiente investigar este campo para refrescar el token de
    // autenticación
    @Schema(name = "refreshToken", example = "eyJ...", description = "Token de actualización JWT utilizado para obtener un nuevo token de acceso")
    private String refreshToken;

    @Schema(name = "expiresIn", example = "900", description = "Tiempo de expiración del token de acceso en segundos")
    private long expiresIn;
}