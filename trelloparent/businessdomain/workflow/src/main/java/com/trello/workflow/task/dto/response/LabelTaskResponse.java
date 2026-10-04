package com.trello.workflow.task.dto.response;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.enums.Status;
import com.trello.workflow.label.dto.response.LabelResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LabelTaskResponse", description = "Representa una tarea en la base de datos e incluye los datos de las etiquetas")
public class LabelTaskResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la tarea en la base de datos")
    private UUID id;

    @Schema(name = "name", example = "Nombre de prueba", description = "Nombre de la tarea en la base de datos")
    private String name;

    @Schema(name = "description", example = "Descripción de prueba", description = "Descripción de la tarea en la base de datos")
    private String description;

    @Schema(name = "status", example = "PENDING", description = "Estado de la tarea en la base de datos")
    private Status status;

    @Schema(name = "labels", description = "Lista de etiquetas asociadas a la tarea")
    private List<LabelResponse> labels;
}
