package com.trello.workflow.label.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ColorResponse", description = "Representa un color en la base de datos")
public class ColorResponse {
    @Schema(name = "id", example = "RED", description = "Identificador del color")
    private String id;
    @Schema(name = "name", example = "Rojo", description = "Nombre del color en español")
    private String name;
    @Schema(name = "hex", example = "#FFC9C9", description = "Código hexadecimal del color")
    private String hex;
}
