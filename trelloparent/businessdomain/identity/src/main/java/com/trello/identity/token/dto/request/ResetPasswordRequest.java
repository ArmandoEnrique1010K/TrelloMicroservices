package com.trello.identity.token.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "ResetPasswordRequest", description = "Representa un token secreto y la nueva contraseña del usuario")
public class ResetPasswordRequest {
    @Schema(name = "newPassword", requiredMode = RequiredMode.REQUIRED, example = "********", description = "Nueva contraseña del usuario")
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener como mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "La nueva contraseña debe contener al menos una mayúscula, un número y un símbolo")
    private String newPassword;

    @Schema(name = "newPasswordConfirmation", requiredMode = RequiredMode.REQUIRED, example = "********", description = "La misma nueva contraseña del usuario")
    @NotBlank(message = "Confirme su nueva contraseña")
    private String newPasswordConfirmation;

    @Schema(name = "resetToken", requiredMode = RequiredMode.REQUIRED, example = "f35...", description = "Token secreto obtenido de la base de datos")
    @NotBlank(message = "El token de reinicio es obligatorio")
    private UUID resetToken;
}