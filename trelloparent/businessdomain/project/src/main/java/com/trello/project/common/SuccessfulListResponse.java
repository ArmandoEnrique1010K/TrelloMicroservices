package com.trello.project.common;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "SuccessfulListResponse", description = "Respuesta listada de exito de la API")
@NoArgsConstructor
@Data
public class SuccessfulListResponse<T> {
    @Schema(name = "message", description = "Mensaje legible para mostrar en el cliente web")
    private String message;

    @Schema(name = "body ", description = "Contenido de la respuesta")
    private List<T> body;

}
