package com.trello.project.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "SuccessfulResponse", description = "Respuesta de exito de la API")
@NoArgsConstructor
@Data
public class SuccessfulResponse<T> {
    @Schema(name = "message", description = "Mensaje legible para mostrar en el cliente web")
    private String message;

    @Schema(name = "body ", description = "Contenido de la respuesta")
    private T body;
}