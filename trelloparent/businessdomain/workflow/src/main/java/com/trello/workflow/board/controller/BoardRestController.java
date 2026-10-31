package com.trello.workflow.board.controller;

import com.trello.workflow.board.service.BoardService;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.security.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Board API", description = "API para los IDs de tableros")
@RestController
@RequestMapping("/boards")
public class BoardRestController {

    private final BoardService boardService;

    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Operation(summary = "Guarda una copia del tablero por su ID", description = "Crea una copia del ID del tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha guardado el permiso"),

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

            @ApiResponse(responseCode = "404", description = "La operación no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The operation was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error",
                        "status": 404,
                        "title": "Operation not found",
                        "type": "/errors/operation/not-found"
                    }
                    """))),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El tablero existe", summary = "El tablero existe", value = """
                            {
                                "detail": "A board with the provided ID already exists",
                                "fields": null,
                                "instance": null,
                                "message": "Ya existe el tablero",
                                "status": 409,
                                "title": "Board already exists",
                                "type": "/errors/board/already-exists"
                            }
                            """),
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
                    @ExampleObject(name = "Se intento ejecutar una operación desconocida", summary = "Se intento ejecutar una operación desconocida", value = """
                            {
                                    "detail": "The operation cannot be completed because it violates a business rule",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error inesperado",
                                    "status": 409,
                                    "title": "Business rule violation",
                                    "type": "/errors/business-rule-violation"
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
    @PostMapping("/{boardId}")
    public ResponseEntity<Void> addBoardAndBoardAccessUserOwner(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @RequestHeader("X-Operation-Id") UUID operationId) {

        UUID userId = JwtUtils.getUserId(jwt);

        boardService.addBoardAndBoardAccessUserOwner(boardId, userId, operationId);

        // Prueba de rendimiento
        // Retrasar la respuesta después del commit del servicio.
        // try {
        // Thread.sleep(4000);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // throw new RuntimeException(e);
        // }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Elimina un tablero por su ID", description = "Elimina la copia del tablero, además de sus permisos de acceso y tareas asociadas")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha eliminado el tablero"),

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
                    @ExampleObject(name = "El tablero no existe", summary = "El tablero no existe", value = """
                            {
                                "detail": "The board was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el tablero",
                                "status": 404,
                                "title": "Board not found",
                                "type": "/errors/board-not-found"
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
                    @ExampleObject(name = "Se intento ejecutar una operación desconocida", summary = "Se intento ejecutar una operación desconocida", value = """
                            {
                                    "detail": "The operation cannot be completed because it violates a business rule",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error inesperado",
                                    "status": 409,
                                    "title": "Business rule violation",
                                    "type": "/errors/business-rule-violation"
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
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoardAndBoardAccessAndTasks(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @RequestHeader("X-Operation-Id") UUID operationId) {

        boardService.deleteBoardAndBoardAccessAndTasks(boardId, operationId);
        // Prueba de rendimiento
        // Retrasar la respuesta después del commit del servicio.
        // try {
        // Thread.sleep(4000);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // throw new RuntimeException(e);
        // }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // Borrar varios tableros por IDs de manera simultanea
    @Operation(summary = "Elimina varios tableros por la lista de IDs", description = "Elimina las copias de los tableros cuando se elimina un espacio de trabajo, además de sus permisos de acceso y tareas asociadas")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha eliminado el espacio de trabajo"),

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
                    @ExampleObject(name = "Uno o varios tableros no existen", summary = "Uno o varios tableros no existen", value = """
                            {
                                "detail": "One or many boards were not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "Uno o varios tableros asociados no fuerón encontrados",
                                "status": 404,
                                "title": "One or many boards not found",
                                "type": "/errors/many-boards-not-found"
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
                    @ExampleObject(name = "Se intento ejecutar una operación desconocida", summary = "Se intento ejecutar una operación desconocida", value = """
                            {
                                    "detail": "The operation cannot be completed because it violates a business rule",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ha ocurrido un error inesperado",
                                    "status": 409,
                                    "title": "Business rule violation",
                                    "type": "/errors/business-rule-violation"
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
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteManyBoardsByIdsAndBoardAccessAndTasks(
            @Parameter(description = "Lista de IDs del tablero", required = false, example = "550e8400-e29b-41d4-a716-446655440000") @RequestParam(required = false) List<UUID> boardIds,
            @RequestHeader("X-Operation-Id") UUID operationId) {
        boardService.deleteAllBoardsByIdsAndBoardAccessAndTasks(boardIds, operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
