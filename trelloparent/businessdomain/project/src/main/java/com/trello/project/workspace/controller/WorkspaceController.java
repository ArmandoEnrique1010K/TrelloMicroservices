package com.trello.project.workspace.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.security.JwtUtils;
import com.trello.project.workspace.dto.request.WorkspaceRequest;
import com.trello.project.workspace.dto.response.WorkspaceResponse;
import com.trello.project.workspace.dto.response.common.SuccessfulWorkspaceResponse;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;
import com.trello.project.workspace.service.WorkspaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Workspace API", description = "API para la gestión de espacios de trabajo por el usuario autenticado")
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Operation(summary = "Añade un nuevo espacio de trabajo", description = "Registra un nuevo espacio de trabajo en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            // Recordar que se va a utilizar un @ExampleObject cuando se devuelva un body y
            // un message en la respuesta
            @ApiResponse(responseCode = "200", description = "Se ha creado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulWorkspaceResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "createdAt": "2026-09-03T21:47:52.333057",
                            "description": "Descripción de prueba para el proyecto",
                            "id": "09901933-c12c-469f-bbfe-b51840f79d15",
                            "name": "Proyecto de prueba"
                        },
                        "message": "Se ha creado el espacio de trabajo"
                    }
                    """))),

            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),

            @ApiResponse(responseCode = "409", description = "El nombre del espacio de trabajo existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A workspace with the provided name already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Existe un espacio de trabajo con ese nombre",
                        "status": 409,
                        "title": "Workspace already exists",
                        "type": "/errors/workspace/already-exists"
                    }
                    """))),

            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @PostMapping
    public ResponseEntity<SuccessfulResponse<WorkspaceResponse>> createAccount(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WorkspaceRequest input)
            throws WorkspaceAlreadyExistsException {
        UUID userId = JwtUtils.getUserId(jwt);

        WorkspaceResponse response = workspaceService.createWorkspace(userId, input);

        SuccessfulResponse<WorkspaceResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha creado el espacio de trabajo");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Lista los espacios de trabajo", description = "Registra un nuevo espacio de trabajo en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            // Las listas se definen en un @ArraySchema pero no se va a poder visualizar el
            // nombre de la clase "WorkspaceResponse" para especificar el tipo de cada
            // elemento de la lista en la sección "Schema" de la UI de Swagger
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de espacios de trabajo del usuario autenticado", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = WorkspaceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> listAllWorkspaces(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<WorkspaceResponse> response = workspaceService.listAllWorkspaces(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha modificado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulWorkspaceResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "createdAt": "2026-09-03T21:47:52.333057",
                            "description": "Descripción modificada",
                            "id": "09901933-c12c-469f-bbfe-b51840f79d15",
                            "name": "Proyecto modificado de prueba"
                        },
                        "message": "Se ha modificado el espacio de trabajo"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "No se ha encontrado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The workspace was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el espacio de trabajo",
                        "status": 400,
                        "title": "Workspace not found",
                        "type": "/errors/workspace-not-found"
                    }
                    """))),

            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),

            @ApiResponse(responseCode = "409", description = "El nombre del espacio de trabajo existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A workspace with the provided name already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Existe un espacio de trabajo con ese nombre",
                        "status": 409,
                        "title": "Workspace already exists",
                        "type": "/errors/workspace/already-exists"
                    }
                    """))),

            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @Operation(summary = "Edita un espacio de trabajo", description = "Edita los datos de un espacio de trabajo en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{workspaceId}")
    public ResponseEntity<SuccessfulResponse<WorkspaceResponse>> editWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del espacio de trabajo", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceRequest input)
            throws WorkspaceNotFoundException, WorkspaceAlreadyExistsException {
        UUID userId = JwtUtils.getUserId(jwt);

        WorkspaceResponse response = workspaceService.editWorkspace(userId, workspaceId, input);

        SuccessfulResponse<WorkspaceResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha modificado el espacio de trabajo");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulWorkspaceResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado el espacio de trabajo"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "No se ha encontrado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The workspace was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el espacio de trabajo",
                        "status": 400,
                        "title": "Workspace not found",
                        "type": "/errors/workspace-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "El usuario no esta autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                      "detail": "An unexpected error occurred while processing the request",
                      "fields": null,
                      "instance": null,
                      "message": "Ha ocurrido un error inesperado",
                      "status": 500,
                      "title": "Internal server error",
                      "type": "/errors/internal-server-error"
                    }
                    """))),
    })
    @Operation(summary = "Elimina un espacio de trabajo", description = "Elimina un espacio de trabajo en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<SuccessfulResponse<WorkspaceResponse>> deleteWorkspace(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del espacio de trabajo", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID workspaceId) {
        UUID userId = JwtUtils.getUserId(jwt);

        workspaceService.deleteWorkspace(userId, workspaceId);

        SuccessfulResponse<WorkspaceResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado el espacio de trabajo");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }
}
