package com.trello.workflow.note.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "NoteRequest", description = "Representa una nota")
public class NoteRequest {
    @Schema(name = "content", requiredMode = RequiredMode.REQUIRED, example = "Nota de prueba", description = "Contenido de la nota")
    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 4, max = 200, message = "El contenido debe tener entre 4 y 200 caracteres")
    private String content;
}
