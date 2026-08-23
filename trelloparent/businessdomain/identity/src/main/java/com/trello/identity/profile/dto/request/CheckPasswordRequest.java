package com.trello.identity.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "CheckPasswordRequest", description = "Representa la contraseña del usuario autenticado")
public class CheckPasswordRequest {

    @Schema(name = "currentPassword", requiredMode = RequiredMode.REQUIRED, example = "********", description = "Contraseña del usuario autenticado")
    @NotBlank(message = "La contraseña es obligatoria")
    private String currentPassword;
}
