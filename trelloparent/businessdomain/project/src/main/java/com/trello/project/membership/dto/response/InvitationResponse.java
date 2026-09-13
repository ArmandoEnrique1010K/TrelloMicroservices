package com.trello.project.membership.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "InvitationResponse", description = "Representa una invitación en la base de datos")
public class InvitationResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la invitación en la base de datos")
    private UUID id;

    // Usuario emisor
    @Schema(name = "senderUserId", example = "f35...", description = "ID del usuario que envia la invitación")
    private UUID senderUserId;

    // Usuario receptor
    @Schema(name = "recipientUserId", example = "f35...", description = "ID del usuario que recibe la invitación")
    private UUID recipientUserId;

    @Schema(name = "message", example = "Unete al tablero como miembro", description = "Mensaje opcional de invitación al usuario")
    private String message;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    // Fecha de creación
    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación del tablero en la base de datos")
    private LocalDateTime createdAt;
}
