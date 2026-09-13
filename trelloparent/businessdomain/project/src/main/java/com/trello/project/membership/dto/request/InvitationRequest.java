package com.trello.project.membership.dto.request;

import com.trello.project.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "InvitationRequest", description = "Representa una invitación")
public class InvitationRequest {
    @Schema(name = "message", example = "Unete al tablero como miembro", description = "Mensaje opcional de invitación al usuario")
    private String message;

    // Los enums no admiten @NotBlank porque no son cadenas.
    // Si se envía un valor que no coincide con ningún valor del enum, la aplicación
    // puede devolver un 500.
    // Por eso se valida con @NotNull y se controla el valor del enum en la lógica
    // de negocio.
    @Schema(name = "role", example = "ADMIN", description = "Rol del usuario")
    @NotNull(message = "El rol es obligatorio")
    private Role role;
}
