package com.trello.identity.token.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "TokenRequest", description = "Representa el token de validación")
public class TokenRequest {
    @Schema(name = "token", requiredMode = RequiredMode.REQUIRED, example = "123456", description = "Token de 6 digitos")
    @NotBlank(message = "El token es obligatorio")
    @Size(min = 6, max = 6, message = "El token debe tener 6 digitos")
    private String token;
}
