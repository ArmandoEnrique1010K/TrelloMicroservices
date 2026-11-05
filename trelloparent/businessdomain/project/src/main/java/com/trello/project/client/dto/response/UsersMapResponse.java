package com.trello.project.client.dto.response;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UsersMapResponse", description = "Representa la lista de usuarios confirmados")
public class UsersMapResponse {
    @Schema(name = "available", example = "true", description = "Indica si se pudo obtener el usuario")
    private boolean available;

    @Schema(name = "usersMap", description = "Lista con los datos de los usuarios indexados por su id")
    private Map<UUID, UserResponse> usersMap;
}