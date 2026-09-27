package com.trello.workflow.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Solamente los usuarios con el rol de ADMIN o el administrador del tablero pueden crear tareas
@Data
@Schema(name = "TaskRequest", description = "Representa una tarea")
public class TaskRequest {
    @Schema(name = "name", requiredMode = RequiredMode.REQUIRED, example = "Tarea de prueba", description = "Nombre de la tarea")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 4, max = 200, message = "El nombre debe tener entre 4 y 200 caracteres")
    private String name;

    @Schema(name = "description", example = "Descripción de la tarea", description = "Descripción breve de la tarea")
    @Size(max = 2000, message = "La descripción debe tener como maximo 2000 caracteres")
    private String description;
}
