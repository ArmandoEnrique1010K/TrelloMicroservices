package com.trello.workflow.client.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserByIdResponse", description = "Representa el usuario confirmado")
public class UserByIdResponse {
    @Schema(name = "available", example = "true", description = "Indica si se pudo obtener el usuario")
    private boolean available;

    @Schema(name = "userById", description = "Datos del usuario")
    private UserResponse userById;
}