package com.trello.workflow.note.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "NoteResponse", description = "Representa una nota en la base de datos")
public class NoteResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la nota en la base de datos")
    private UUID id;

    @Schema(name = "content", example = "Contenido de prueba", description = "Contenido de la nota en la base de datos")
    private String content;

    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación de la nota en la base de datos")
    private LocalDateTime createdAt;
}
