package com.trello.project.member.dto.response;

import java.util.UUID;

import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "MemberResponse", description = "Representa un miembro desde la base de datos")
public class MemberResponse {

    @Schema(name = "id", example = "f35...", description = "ID del miembro en la base de datos")
    private UUID id;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    @Schema(name = "active", example = "true", description = "¿El usuario puede hacer operaciones?")
    private boolean active;
}
