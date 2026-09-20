package com.trello.project.invitation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// Este Response se utiliza cuando el usuario que ha iniciado sesion quiera ver las invitaciones recibidas
@Data
@Schema(name = "ReceivedInvitationResponse", description = "Representa una invitación desde un usuario autenticado en la base de datos")
public class ReceivedInvitationResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la invitación en la base de datos")
    private UUID id;

    @Schema(name = "message", example = "Unete al tablero como miembro", description = "Mensaje opcional de invitación al usuario")
    private String message;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    @Schema(name = "sendedAt", example = "2025-01-15T10:30:45", description = "Fecha de envio de la invitación en la base de datos")
    private LocalDateTime sendedAt;

    @Schema(name = "senderUser", description = "Usuario emisor de la invitación")
    private UserResponse senderUser;

    // Datos del tablero
    // @Schema(name = "boardId", example = "f35...", description = "ID del tablero
    // en la base de datos")
    // private UUID boardId;

    @Schema(name = "boardName", example = "Tablero de prueba", description = "Nombre del tablero en la base de datos")
    private String boardName;

    // Datos del espacio de trabajo
    // @Schema(name = "workspaceId", example = "f35...", description = "ID del
    // espacio de trabajo en la base de datos")
    // private UUID workspaceId;

    @Schema(name = "workspaceName", example = "Proyecto de prueba", description = "Nombre del espacio de trabajo en la base de datos")
    private String workspaceName;
}
