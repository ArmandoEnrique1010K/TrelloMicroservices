package com.trello.workflow.task.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.security.JwtUtils;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.dto.response.common.SuccessfulTaskResponse;
import com.trello.workflow.task.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Task API", description = "API para la gestión de tareas")
@RestController
@RequestMapping("/task")
public class TaskRestController {

    private final TaskService taskService;

    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Agrega una tarea", description = "Agrega una tarea al tablero por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha creado la tarea", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulTaskResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "description": "Tarea creada desde el usuario administrador del espacio de trabajo",
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6",
                            "name": "Tarea de prueba 1",
                            "status": "PENDING",
                            "updatedAt": "2026-09-27T14:54:38.5068766"
                        },
                        "message": "Se ha creado la tarea"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "name": "El nombre debe tener entre 4 y 200 caracteres"
                        },
                        "instance": null,
                        "message": "Complete los campos indicados",
                        "status": 400,
                        "title": "Invalid request",
                        "type": "/errors/validation"
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
            @ApiResponse(responseCode = "403", description = "El usuario no tiene el rol requerido para realizar la operación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The user does not have the required role to perform the operation",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error",
                        "status": 403,
                        "title": "Forbidden operation",
                        "type": "/errors/forbidden-operation"
                    }
                    """))),

            @ApiResponse(responseCode = "404", description = "El permiso de acceso al tablero no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The board access was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el permiso de acceso al tablero",
                        "status": 404,
                        "title": "Board access not found",
                        "type": "/errors/board-access-not-found"
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
    @PostMapping("/board/{boardId}")
    public ResponseEntity<SuccessfulResponse<TaskResponse>> createTask(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Valid @RequestBody TaskRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);

        TaskResponse response = taskService.createTask(input, boardId, userId);
        SuccessfulResponse<TaskResponse> successfulResponse = new SuccessfulResponse<>();

        successfulResponse.setMessage("Se ha creado la tarea");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

}
