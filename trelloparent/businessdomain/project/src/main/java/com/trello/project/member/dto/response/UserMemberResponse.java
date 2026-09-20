package com.trello.project.member.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserMemberResponse", description = "Representa un miembro con los datos del usuario en la base de datos")
public class UserMemberResponse {

    @Schema(name = "id", example = "f35...", description = "ID del miembro en la base de datos")
    private UUID id;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    @Schema(name = "joinedAt", example = "2025-01-15T10:30:45", description = "Fecha de registro del miembro")
    private LocalDateTime joinedAt;

    @Schema(name = "active", example = "true", description = "¿El usuario puede hacer operaciones?")
    private boolean active;

    @Schema(name = "memberUser", description = "Usuario miembro")
    private UserResponse memberUser;

}
