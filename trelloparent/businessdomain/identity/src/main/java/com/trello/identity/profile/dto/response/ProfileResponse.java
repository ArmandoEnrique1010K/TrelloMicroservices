package com.trello.identity.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ProfileResponse", description = "Representa el perfil del usuario autenticado")
public class ProfileResponse {
    @Schema(name = "firstName", example = "John", description = "Nombres del usuario en la base de datos")
    private String firstName;
    @Schema(name = "lastName", example = "Doe", description = "Apellidos del usuario en la base de datos")
    private String lastName;
    @Schema(name = "email", example = "example@gmail.com", description = "Correo del usuario en la base de datos")
    private String email;
}
