package com.trello.project.membership.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// Este Response se utiliza cuando el usuario administrador del tablero quiera ver las invitaciones enviadas
@Data
@Schema(name = "BoardInvitationResponse", description = "Representa una invitación desde un tablero en la base de datos")
public class BoardInvitationResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la invitación en la base de datos")
    private UUID id;

    @Schema(name = "message", example = "Unete al tablero como miembro", description = "Mensaje opcional de invitación al usuario")
    private String message;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    @Schema(name = "sendedAt", example = "2025-01-15T10:30:45", description = "Fecha de envio de la invitación en la base de datos")
    private LocalDateTime sendedAt;

    @Schema(name = "status", example = "ACCEPTED", description = "Estado de la invitación")
    private Status status;

    // DTO para mapear los datos del usuario obtenido
    // El usuario a quien se le envio la invitación (receptor)
    @Schema(name = "recipientUser", description = "Usuario receptor de la invitación")
    private UserResponse recipientUser;
}
