package com.trello.project.workspace.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "WorkspaceRequest", description = "Representa un espacio de trabajo")
public class WorkspaceRequest {
    @Schema(name = "name", requiredMode = RequiredMode.REQUIRED, example = "Proyecto de prueba", description = "Nombre del espacio de trabajo")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre debe tener entre 4 y 50 caracteres")
    private String name;

    @Schema(name = "description", requiredMode = RequiredMode.REQUIRED, example = "Descripción de prueba para el proyecto", description = "Descripción del espacio de trabajo")
    @Size(max = 1000, message = "La descripción no debe exceder de 1000 caracteres")
    private String description;
}
