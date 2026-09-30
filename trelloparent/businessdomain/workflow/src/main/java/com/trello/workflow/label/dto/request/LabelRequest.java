package com.trello.workflow.label.dto.request;

import com.trello.workflow.enums.Color;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "LabelRequest", description = "Representa una etiqueta")
public class LabelRequest {

    @Schema(name = "content", requiredMode = RequiredMode.REQUIRED, example = "Etiqueta de prueba", description = "Nombre de la tarea")
    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 4, max = 50, message = "El contenido debe tener entre 4 y 50 caracteres")
    private String content;

    @Schema(name = "color", example = "FFF000", description = "Color de la etiqueta")
    private Color color;

}
