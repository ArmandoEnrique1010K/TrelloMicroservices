package com.trello.workflow.task.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.trello.workflow.client.dto.response.UserResponse;
import com.trello.workflow.enums.Status;
import com.trello.workflow.history.dto.response.HistoryResponse;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.note.dto.response.UserNoteResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "DetailsTaskResponse", description = "Representa una tarea en la base de datos con los datos del usuario")
public class DetailsTaskResponse {

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

    // Información de los datos de usuario
    @Schema(name = "createdByUser", description = "Usuario creador de la tarea")
    private UserResponse createdByUser;

    // Etiquetas
    @Schema(name = "labels", description = "Lista de etiquetas asociadas a la tarea")
    private List<LabelResponse> labels;

    // Notas
    @Schema(name = "notes", description = "Lista de notas asociadas a la tarea")
    private List<UserNoteResponse> notes;

    // Historial
    @Schema(name = "histories", description = "Lista de historial de estados de la tarea")
    private List<HistoryResponse> histories;
}
