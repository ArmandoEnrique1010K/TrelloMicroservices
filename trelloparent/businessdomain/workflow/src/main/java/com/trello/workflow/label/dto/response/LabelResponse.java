package com.trello.workflow.label.dto.response;

import java.util.UUID;

import com.trello.workflow.enums.Color;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LabelResponse", description = "Representa una etiqueta en la base de datos")
public class LabelResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la tarea en la base de datos")
    private UUID id;

    @Schema(name = "content", example = "Etiqueta de prueba", description = "Nombre de la tarea en la base de datos")
    private String content;

    // Color amarillo, en este caso se especifica el enum
    @Schema(name = "color", example = "YELLOW", description = "Color de la etiqueta en la base de datos")
    private Color color;
}
