package com.trello.project.workspace.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BoardResponse", description = "Representa el tablero en la base de datos")
public class BoardResponse {
    @Schema(name = "id", example = "f35...", description = "ID del tablero en la base de datos")
    private UUID id;
    @Schema(name = "name", example = "Tablero de prueba", description = "Nombre del tablero en la base de datos")
    private String name;
    @Schema(name = "description", example = "Descripción de prueba para el tablero", description = "Descripción del tablero en la base de datos")
    private String description;
    @Schema(name = "createdAt", example = "2025-01-15T10:30:45", description = "Fecha de creación del tablero en la base de datos")
    private LocalDateTime createdAt;

}
