package com.trello.workflow.task.dto.response;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.enums.Status;
import com.trello.workflow.label.dto.response.LabelResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// Se utilizara este response en la lista de tareas obtenidas
@Data
@Schema(name = "AuthorTaskResponse", description = "Representa una tarea en la base de datos e incluye un campo para verificar si es el autor de la tarea")

public class AuthorTaskResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la tarea en la base de datos")
    private UUID id;

    @Schema(name = "name", example = "Nombre de prueba", description = "Nombre de la tarea en la base de datos")
    private String name;

    @Schema(name = "description", example = "Descripción de prueba", description = "Descripción de la tarea en la base de datos")
    private String description;

    @Schema(name = "status", example = "PENDING", description = "Estado de la tarea en la base de datos")
    private Status status;

    // Campo para verificar si es el autor de la tarea
    @Schema(name = "author", example = "true", description = "Verifica si el usuario autenticado es el autor de la tarea en la base de datos")
    private boolean author;

    // Lista de etiquetas, cada elemento son de tipo LabelResponse
    @Schema(name = "labels", description = "Lista de etiquetas asociadas a la tarea")
    private List<LabelResponse> labels;
}
