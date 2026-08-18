package com.trello.identity.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "AuthenticationRequest", description = "Represents the credentials of authentication")
public class AuthenticationRequest {
    @Schema(name = "email", requiredMode = RequiredMode.REQUIRED, example = "example@gmail.com", description = "Email of the user")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene el formato adecuado")
    private String email;

    @Schema(name = "password", requiredMode = RequiredMode.REQUIRED, example = "********", description = "Password of the user")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
