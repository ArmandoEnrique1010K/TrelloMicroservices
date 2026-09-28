package com.trello.workflow.task.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.workflow.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "TaskResponse", description = "Representa una tarea en la base de datos")
public class TaskResponse {

    @Schema(name = "id", example = "f35...", description = "ID de la tarea en la base de datos")
    private UUID id;

    @Schema(name = "name", example = "Nombre de prueba", description = "Nombre de la tarea en la base de datos")
    private String name;

    @Schema(name = "description", example = "Descripción de prueba", description = "Descripción de la tarea en la base de datos")
    private String description;

    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación de la tarea en la base de datos")
    private LocalDateTime createdAt;

    @Schema(name = "updatedAt", example = "2025-01-15T10:30:45", description = "Fecha de actualización de la tarea en la base de datos")
    private LocalDateTime updatedAt;

    @Schema(name = "status", example = "PENDING", description = "Estado de la tarea en la base de datos")
    private Status status;
}
