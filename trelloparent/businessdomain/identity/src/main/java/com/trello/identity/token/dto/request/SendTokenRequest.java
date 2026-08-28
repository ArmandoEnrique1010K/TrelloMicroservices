package com.trello.identity.token.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "SendTokenRequest", description = "Representa el correo del usuario al que se le enviara un token de validación")
public class SendTokenRequest {
    @Schema(name = "email", requiredMode = RequiredMode.REQUIRED, example = "example@gmail.com", description = "Correo del usuario")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene el formato adecuado")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com)$", message = "El correo debe pertenecer a Gmail, Hotmail u Outlook")
    private String email;
}
