package com.trello.project.client.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UsersListResponse", description = "Representa una lista de usuarios en la base de datos")
public class UsersListResponse {
    @Schema(name = "available", example = "true", description = "Indica si se pudo obtener el usuario")
    private boolean available;

    @Schema(name = "users", description = "Lista con los datos de los usuarios")
    private List<UserResponse> users;
}
