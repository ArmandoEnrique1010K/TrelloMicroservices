package com.trello.identity.user.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserResponse", description = "Representa un usuario en la base de datos")
public class UserResponse {
    @Schema(name = "id", example = "f35...", description = "ID del usuario en la base de datos")
    private UUID id;
    @Schema(name = "firstName", example = "John", description = "Nombres del usuario en la base de datos")
    private String firstName;
    @Schema(name = "lastName", example = "Doe", description = "Apellidos del usuario en la base de datos")
    private String lastName;
    @Schema(name = "email", example = "example@gmail.com", description = "Correo del usuario en la base de datos")
    private String email;
}
