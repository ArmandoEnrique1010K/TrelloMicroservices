package com.trello.project.board.dto.response;

import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BoardMembershipResponse", description = "Representa el tablero del usuario en el que es miembro en la base de datos")
public class BoardMembershipResponse {
    @Schema(name = "id", example = "f35...", description = "ID del tablero en la base de datos")
    private UUID id;
    @Schema(name = "name", example = "Tablero de prueba", description = "Nombre del tablero en la base de datos")
    private String name;
    @Schema(name = "description", example = "Descripción de prueba para el tablero", description = "Descripción del tablero en la base de datos")
    private String description;
    // Mapear el rol del miembro
    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    // Usuario autor del espacio de trabajo que es el mismo que el autor del tablero
    @Schema(name = "userDataAvailable", example = "true", description = "Confirmación si se puede obtener los datos del usuario")
    private boolean userDataAvailable;
    @Schema(name = "ownerUser", description = "Usuario autor del espacio de trabajo")
    private UserResponse ownerUser;
}
