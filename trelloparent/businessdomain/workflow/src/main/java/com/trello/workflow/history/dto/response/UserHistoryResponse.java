package com.trello.workflow.history.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.workflow.client.dto.response.UserResponse;
import com.trello.workflow.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserHistoryResponse", description = "Representa una historia del estado de una tarea con los datos del usuario en la base de datos")
public class UserHistoryResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la historia en la base de datos")
    private UUID id;
    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación de la nota en la base de datos")
    private LocalDateTime createdAt;
    @Schema(name = "status", example = "PENDING", description = "Estado de la tarea en ese tiempo en la base de datos")
    private Status status;
    @Schema(name = "createdByUser", description = "Usuario editor del estado de la tarea en la base de datos")
    private UserResponse createdByUser;
    // ¿Se pudieron obtener los datos del usuario?
    @Schema(name = "userDataAvailable", example = "true", description = "Confirmación si se puede obtener los datos del usuario")
    private boolean userDataAvailable;
}
