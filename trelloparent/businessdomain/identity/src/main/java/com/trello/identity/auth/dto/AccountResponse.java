package com.trello.identity.auth.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AccountResponse", description = "Representa al usuario en la base de datos")
public class AccountResponse {
    @Schema(name = "id", example = "f35...", description = "ID del usuario en la base de datos")
    private UUID id;
    @Schema(name = "email", example = "example@gmail.com", description = "Correo del usuario en la base de datos")
    private String email;
    @Schema(name = "confirmed", example = "false", description = "Estado de confirmación del usuario en la base de datos")
    private boolean confirmed;
}
