package com.trello.identity.token.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ValidatePasswordResetTokenResponse", description = "Información resultante del proceso de validación del token de cambio de contraseña")
public class ValidatePasswordResetTokenResponse {
    @Schema(name = "resetToken", example = "f35...", description = "Token secreto obtenido desde la base de datos")
    private UUID resetToken;
}
