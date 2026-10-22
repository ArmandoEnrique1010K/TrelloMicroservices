package com.trello.workflow.operation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.operation.service.handler.OperationActivateBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationAddBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationChangeRoleBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationDeactivateBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationDeleteAllBoardsAndBoardAccessAndTasksService;
import com.trello.workflow.operation.service.handler.OperationDeleteBoardAndBoardAccessAndTasksService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Operation API", description = "API para la gestión de operaciones")
@RestController
@RequestMapping("/operations")
public class OperationController {

    private final OperationAddBoardAccessService operationAddBoardAccessService;
    private final OperationActivateBoardAccessService operationActivateBoardAccessService;
    private final OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService;
    private final OperationDeactivateBoardAccessService operationDeactivateBoardAccessService;
    private final OperationDeleteBoardAndBoardAccessAndTasksService operationDeleteBoardAndBoardAccessAndTasksService;
    private final OperationDeleteAllBoardsAndBoardAccessAndTasksService operationDeleteAllBoardsAndBoardAccessAndTasksService;

    public OperationController(OperationAddBoardAccessService operationAddBoardAccessService,
            OperationActivateBoardAccessService operationActivateBoardAccessService,
            OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService,
            OperationDeactivateBoardAccessService operationDeactivateBoardAccessService,
            OperationDeleteBoardAndBoardAccessAndTasksService operationDeleteBoardAndBoardAccessAndTasksService,
            OperationDeleteAllBoardsAndBoardAccessAndTasksService operationDeleteAllBoardsAndBoardAccessAndTasksService) {
        this.operationAddBoardAccessService = operationAddBoardAccessService;
        this.operationActivateBoardAccessService = operationActivateBoardAccessService;
        this.operationChangeRoleBoardAccessService = operationChangeRoleBoardAccessService;
        this.operationDeactivateBoardAccessService = operationDeactivateBoardAccessService;
        this.operationDeleteBoardAndBoardAccessAndTasksService = operationDeleteBoardAndBoardAccessAndTasksService;
        this.operationDeleteAllBoardsAndBoardAccessAndTasksService = operationDeleteAllBoardsAndBoardAccessAndTasksService;
    }

    // Endpoints relacionados a reversion de operaciones
    @Operation(summary = "Revierte una operación de tipo guardar un permiso de acceso al tablero", description = "Revierte los cambios realizados en la base de datos luego de guardar un permiso de acceso al tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha revertido la operación"),

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
                    @ExampleObject(name = "La operación no existe", summary = "La operación no existe", value = """
                            {
                                "detail": "The operation was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 404,
                                "title": "Operation not found",
                                "type": "/errors/operation/not-found"
                            }
                            """),
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-accesses-not-found"
                            }
                            """),
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "La operación no tiene un estado pendiente", summary = "La operación no tiene un estado pendiente", value = """
                            {
                                    "detail": "The operation does not have a 'PENDING' or relationated status",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation not pending",
                                    "type": "/errors/operation/not-pending"
                            }
                            """),
                    @ExampleObject(name = "Los datos enviados no coinciden con los datos de la operación", summary = "Los datos enviados no coinciden con los datos de la operación", value = """
                            {
                                    "detail": "The data related to the operation do not match the requested data",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation in conflict",
                                    "type": "/errors/operation/conflict"
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
    @PostMapping("/revert-add-board-access")
    public ResponseEntity<Void> revertAddBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationAddBoardAccessService.revertAddBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Revierte una operación de tipo activar un permiso de acceso al tablero", description = "Revierte los cambios realizados en la base de datos luego de activar un permiso de acceso al tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha revertido la operación"),

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
                    @ExampleObject(name = "La operación no existe", summary = "La operación no existe", value = """
                            {
                                "detail": "The operation was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 404,
                                "title": "Operation not found",
                                "type": "/errors/operation/not-found"
                            }
                            """),
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-accesses-not-found"
                            }
                            """),
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "La operación no tiene un estado pendiente", summary = "La operación no tiene un estado pendiente", value = """
                            {
                                    "detail": "The operation does not have a 'PENDING' or relationated status",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation not pending",
                                    "type": "/errors/operation/not-pending"
                            }
                            """),
                    @ExampleObject(name = "Los datos enviados no coinciden con los datos de la operación", summary = "Los datos enviados no coinciden con los datos de la operación", value = """
                            {
                                    "detail": "The data related to the operation do not match the requested data",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation in conflict",
                                    "type": "/errors/operation/conflict"
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
    @PostMapping("/revert-activate-board-access")
    public ResponseEntity<Void> revertActivateBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationActivateBoardAccessService.revertActivateBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Revierte una operación de tipo cambiar el rol de un permiso de acceso al tablero", description = "Revierte los cambios realizados en la base de datos luego de cambiar el rol de un permiso de acceso al tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha revertido la operación"),

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
                    @ExampleObject(name = "La operación no existe", summary = "La operación no existe", value = """
                            {
                                "detail": "The operation was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 404,
                                "title": "Operation not found",
                                "type": "/errors/operation/not-found"
                            }
                            """),
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-accesses-not-found"
                            }
                            """),
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "La operación no tiene un estado pendiente", summary = "La operación no tiene un estado pendiente", value = """
                            {
                                    "detail": "The operation does not have a 'PENDING' or relationated status",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation not pending",
                                    "type": "/errors/operation/not-pending"
                            }
                            """),
                    @ExampleObject(name = "Los datos enviados no coinciden con los datos de la operación", summary = "Los datos enviados no coinciden con los datos de la operación", value = """
                            {
                                    "detail": "The data related to the operation do not match the requested data",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation in conflict",
                                    "type": "/errors/operation/conflict"
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
    @PostMapping("/revert-change-board-access")
    public ResponseEntity<Void> revertChangeBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationChangeRoleBoardAccessService.revertChangeRoleBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Revierte una operación de tipo cambiar el rol de un permiso de acceso al tablero", description = "Revierte los cambios realizados en la base de datos luego de cambiar el rol de un permiso de acceso al tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha revertido la operación"),

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
                    @ExampleObject(name = "La operación no existe", summary = "La operación no existe", value = """
                            {
                                "detail": "The operation was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 404,
                                "title": "Operation not found",
                                "type": "/errors/operation/not-found"
                            }
                            """),
                    @ExampleObject(name = "El permiso de acceso al tablero no existe", summary = "El permiso de acceso al tablero no existe", value = """
                            {
                                "detail": "The board access was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el permiso de acceso al tablero",
                                "status": 404,
                                "title": "Board access not found",
                                "type": "/errors/board-accesses-not-found"
                            }
                            """),
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "La operación no tiene un estado pendiente", summary = "La operación no tiene un estado pendiente", value = """
                            {
                                    "detail": "The operation does not have a 'PENDING' or relationated status",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation not pending",
                                    "type": "/errors/operation/not-pending"
                            }
                            """),
                    @ExampleObject(name = "Los datos enviados no coinciden con los datos de la operación", summary = "Los datos enviados no coinciden con los datos de la operación", value = """
                            {
                                    "detail": "The data related to the operation do not match the requested data",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error",
                                    "status": 409,
                                    "title": "Operation in conflict",
                                    "type": "/errors/operation/conflict"
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
    @PostMapping("/revert-deactivate-board-access")
    public ResponseEntity<Void> revertDeactivateBoardAccess(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationDeactivateBoardAccessService.revertDeactivateBoardAccess(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // TODO: AÑADIR LAS @ApiResponses
    @Operation(summary = "Revierte una operación de tipo eliminar tablero junto con permisos de acceso y tareas", description = "Revierte los cambios realizados en la base de datos luego de cambiar el estado de un tablero y sus permisos de acceso")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/revert-delete-board-and-board-access-and-tasks")
    public ResponseEntity<Void> revertDeleteBoardAndBoardAccessAndTasks(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationDeleteBoardAndBoardAccessAndTasksService.revertDeleteBoardAndBoardAccessAndTasks(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // TODO: AÑADIR LAS @ApiResponses
    @Operation(summary = "Revierte una operación de tipo eliminar varios tableros junto con sus respectivos permisos de acceso y tareas", description = "Revierte los cambios realizados en la base de datos luego de cambiar el estado de una lista de tableros y sus permisos de acceso")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/revert-delete-all-boards-and-board-access-and-tasks")
    public ResponseEntity<Void> revertDeleteAllBoardsAndBoardAccessAndTasks(
            @RequestHeader("X-Operation-Id") UUID operationId) {
        operationDeleteAllBoardsAndBoardAccessAndTasksService.revertDeleteAllBoardsAndBoardAccessAndTasks(operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}