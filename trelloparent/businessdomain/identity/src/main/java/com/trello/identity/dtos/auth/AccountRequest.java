package com.trello.identity.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "AccountRequest", description = "Represents a user account to register")
public class AccountRequest {
    @Schema(name = "firstName", requiredMode = RequiredMode.REQUIRED, example = "John", description = "FirstName of the user")
    @NotBlank(message = "Su nombre es obligatorio")
    @Size(min = 3, max = 25, message = "El nombre debe tener entre 3 y 25 caracteres")
    private String firstName;

    @Schema(name = "lastName", requiredMode = RequiredMode.REQUIRED, example = "Doe", description = "LastName of the user")
    @NotBlank(message = "Su apellido es obligatorio")
    @Size(min = 3, max = 25, message = "El apellido debe tener entre 3 y 25 caracteres")
    private String lastName;

    @Schema(name = "email", requiredMode = RequiredMode.REQUIRED, example = "example@gmail.com", description = "Email of the user")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene el formato adecuado")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com)$", message = "El correo debe pertenecer a Gmail, Hotmail u Outlook")
    private String email;

    @Schema(name = "password", requiredMode = RequiredMode.REQUIRED, example = "********", description = "Password of the user")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener como mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "La contraseña debe contener al menos una mayúscula, un número y un símbolo")
    private String password;

    @Schema(name = "passwordConfirmation", requiredMode = RequiredMode.REQUIRED, example = "********", description = "The same password of the user")
    @NotBlank(message = "Confirme su contraseña")
    private String passwordConfirmation;
}
