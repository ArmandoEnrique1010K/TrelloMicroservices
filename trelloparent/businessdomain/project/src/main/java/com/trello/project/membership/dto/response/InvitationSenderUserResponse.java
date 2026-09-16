package com.trello.project.membership.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// Esto se mostrara en la vista del administrador del tablero
@Data
@Schema(name = "InvitationSenderUserResponse", description = "Representa una invitación desde un usuario emisor en la base de datos")
public class InvitationSenderUserResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la invitación en la base de datos")
    private UUID id;

    // X - usuario emisor
    // @Schema(name = "senderUserId", example = "f35...", description = "ID del
    // usuario que envia la invitación")
    // private UUID senderUserId;

    @Schema(name = "message", example = "Unete al tablero como miembro", description = "Mensaje opcional de invitación al usuario")
    private String message;

    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    private Role role;

    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación del tablero en la base de datos")
    private LocalDateTime createdAt;

    // Para el administrador el tablero, es necesario que observe estos campos
    @Schema(name = "status", example = "ACCEPTED", description = "Estado de la invitación")
    private Status status;


    // Se utiliza DTOs en lugar de texto plano
    @Schema(name = "senderUser", description = "Usuario emisor de la invitación")
    private UserResponse senderUser;
    
    @Schema(name = "recipientUser", description = "Usuario receptor de la invitación")
    private UserResponse recipientUser;
}
