package com.trello.workflow.task.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.common.SuccessfulVoidResponse;
import com.trello.workflow.enums.Status;
import com.trello.workflow.security.JwtUtils;
import com.trello.workflow.task.dto.request.TaskRequest;
import com.trello.workflow.task.dto.response.TaskResponse;
import com.trello.workflow.task.dto.response.common.SuccessfulTaskResponse;
import com.trello.workflow.task.service.TaskService;

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

@Tag(name = "Task API", description = "API para la gestión de tareas")
@RestController
@RequestMapping("/task")
public class TaskRestController {

    private final TaskService taskService;

    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    // TODO: PROBAR SI VERIFICA EL AUTOR DE LA TAREA EN EL METODO DE CREAR, EDITAR Y
    // ELIMINAR
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
                            "createdAt": "2026-09-27T14:54:38.5068766",
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

    @Operation(summary = "Lista las tareas", description = "Obtiene una lista de tareas por ID del tablero en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de tareas por ID de tablero", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),

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
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<TaskResponse>> listAllTasksByBoardId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);
        List<TaskResponse> response = taskService.listAllTasksByBoardId(boardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Edita una tarea", description = "Edita los datos de una tarea por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha modificado la tarea", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulTaskResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "description": "Tarea creada desde el usuario administrador del espacio de trabajo",
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6",
                            "name": "Tarea de prueba 1",
                            "status": "PENDING",
                            "createdAt": "2026-09-27T14:54:38.5068766",
                            "updatedAt": "2026-09-27T14:54:38.5068766"
                        },
                        "message": "Se ha modificado la tarea"
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

            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para realizar esta operación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El usuario no es el autor del recurso solicitado", summary = "El usuario no es el autor del recurso solicitado", value = """
                                {
                                    "detail": "The user is not the author of the requested resource",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 403,
                                    "title": "Mismached Author",
                                    "type": "/errors/mismached-author"
                                }
                            """),
                    @ExampleObject(name = "El usuario no tiene el rol requerido para realizar la operación", summary = "El usuario no tiene el rol requerido para realizar la operación", value = """
                                {
                                    "detail": "The user does not have the required role to perform the operation",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 403,
                                    "title": "Forbidden operation",
                                    "type": "/errors/forbidden-operation"
                                }
                            """)
            })),

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-access-not-found"
                            }
                            """),
                    @ExampleObject(name = "La tarea no existe", summary = "La tarea no existe", value = """
                            {
                                "detail": "The task was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la tarea",
                                "status": 404,
                                "title": "Task not found",
                                "type": "/errors/task-not-found"
                            }
                            """),

            })),
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
    @PutMapping("/{taskId}")
    public ResponseEntity<SuccessfulResponse<TaskResponse>> editTask(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de tarea", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);
        TaskResponse response = taskService.editTask(taskId, input, userId);

        SuccessfulResponse<TaskResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha modificado la tarea");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Edita el estado de una tarea", description = "Cambia el estado de una tarea por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha modificado la tarea", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulTaskResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "description": "Tarea creada desde el usuario administrador del espacio de trabajo",
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6",
                            "name": "Tarea de prueba 1",
                            "status": "PENDING",
                            "createdAt": "2026-09-27T14:54:38.5068766",
                            "updatedAt": "2026-09-27T14:54:38.5068766"
                        },
                        "message": "Se ha modificado la tarea"
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

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-access-not-found"
                            }
                            """),
                    @ExampleObject(name = "La tarea no existe", summary = "La tarea no existe", value = """
                            {
                                "detail": "The task was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la tarea",
                                "status": 404,
                                "title": "Task not found",
                                "type": "/errors/task-not-found"
                            }
                            """),
            })),
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
    @PatchMapping("/{taskId}/{statusName}")
    public ResponseEntity<SuccessfulResponse<TaskResponse>> changeStatusTask(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de tarea", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID taskId,
            @Parameter(description = "Estado de tarea", required = true, example = "IN_PROGRESS") @PathVariable Status statusName) {
        UUID userId = JwtUtils.getUserId(jwt);
        TaskResponse response = taskService.changeStatusTask(taskId, statusName, userId);

        SuccessfulResponse<TaskResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha cambiado el estado de la tarea");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Elimina una tarea", description = "Elimina una tarea por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado la tarea", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado la tarea"
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

            @ApiResponse(responseCode = "403", description = "", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El usuario no es el autor del recurso solicitado", summary = "El usuario no es el autor del recurso solicitado", value = """
                                {
                                    "detail": "The user is not the author of the requested resource",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 403,
                                    "title": "Mismached Author",
                                    "type": "/errors/mismached-author"
                                }
                            """),
                    @ExampleObject(name = "El usuario no tiene el rol requerido para realizar la operación", summary = "El usuario no tiene el rol requerido para realizar la operación", value = """
                                {
                                    "detail": "The user does not have the required role to perform the operation",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 403,
                                    "title": "Forbidden operation",
                                    "type": "/errors/forbidden-operation"
                                }
                            """)
            })),

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-access-not-found"
                            }
                            """),
                    @ExampleObject(name = "La tarea no existe", summary = "La tarea no existe", value = """
                            {
                                "detail": "The task was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la tarea",
                                "status": 404,
                                "title": "Task not found",
                                "type": "/errors/task-not-found"
                            }
                            """),
            })),
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
    // TODO: PROBAR ESTE MÉTODO SI BORRA TODAS LAS ENTIDADES QUE ESTAN CONECTADAS
    @DeleteMapping("/{taskId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> deleteTask(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de tarea", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID taskId) {
        UUID userId = JwtUtils.getUserId(jwt);
        taskService.deleteTask(taskId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado la tarea");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}
