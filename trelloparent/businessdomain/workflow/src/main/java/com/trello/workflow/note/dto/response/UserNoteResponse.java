package com.trello.workflow.note.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.workflow.client.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserNoteResponse", description = "Representa una nota con los datos del usuario en la base de datos")
public class UserNoteResponse {
    @Schema(name = "id", example = "f35...", description = "ID de la nota en la base de datos")
    private UUID id;

    @Schema(name = "content", example = "Contenido de prueba", description = "Contenido de la nota en la base de datos")
    private String content;

    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación de la nota en la base de datos")
    private LocalDateTime createdAt;

    @Schema(name = "createdByUser", description = "Usuario creador de la nota")
    private UserResponse createdByUser;
}
