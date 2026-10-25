package com.trello.workflow.boardaccess.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.boardaccess.service.BoardAccessService;
import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.enums.Role;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Board Access API", description = "API para la gestión de permisos de acceso al tablero por usuarios")
@RestController
@RequestMapping("/board-accesses")
public class BoardAccessRestController {

    private final BoardAccessService boardAccessService;

    public BoardAccessRestController(BoardAccessService boardAccessService) {
        this.boardAccessService = boardAccessService;
    }

    @Operation(summary = "Guarda un permiso de acceso al tablero", description = "Crea una copia local de los datos de acceso")
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
                    @ExampleObject(name = "El permiso de acceso al tablero existe", summary = "El permiso de acceso al tablero existe", value = """
                            {
                                    "detail": "A board access with the provided board ID and user ID already exists",
                                    "fields": null,
                                    "instance": null,
                                    "message": "Ya existe un permiso de acceso al tablero",
                                    "status": 409,
                                    "title": "Board access already exists",
                                    "type": "/errors/board-accesses/already-exists"
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
    @PostMapping("/board/{boardId}/role/{roleName}")
    public ResponseEntity<Void> addBoardAccess(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "Rol del miembro", required = true, example = "OWNER") @PathVariable Role roleName,
            @RequestHeader("X-Operation-Id") UUID operationId) {

        UUID userId = JwtUtils.getUserId(jwt);

        boardAccessService.addBoardAccess(boardId, userId, roleName, operationId);

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

    @Operation(summary = "Activa un permiso de acceso al tablero", description = "Modifica el campo active de la copia local de los datos de acceso")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha activado el permiso de acceso"),

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

    @PatchMapping("/activate/board/{boardId}/user/{memberUserId}/role/{roleName}/old-role/{oldRoleName}")
    public ResponseEntity<Void> activateBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId,
            @Parameter(description = "Rol nuevo del miembro", required = true, example = "ADMIN") @PathVariable Role roleName,
            @Parameter(description = "Rol antiguo del miembro", required = true, example = "ADMIN") @PathVariable Role oldRoleName,
            @RequestHeader("X-Operation-Id") UUID operationId) {
        boardAccessService.activateBoardAccess(boardId, memberUserId, roleName, oldRoleName, operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Cambia el rol de un permiso de acceso al tablero", description = "Modifica el rol de la copia local de los datos de acceso")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha cambiado el rol del permiso de acceso"),

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
    @PutMapping("/board/{boardId}/user/{memberUserId}/role/{roleName}/old-role/{oldRoleName}")
    // El ID del usuario que es el miembro, se obtiene desde un parametro
    public ResponseEntity<Void> changeRoleBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario miembro", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId,
            @Parameter(description = "Rol nuevo del miembro", required = true, example = "ADMIN") @PathVariable Role roleName,
            @Parameter(description = "Rol antiguo del miembro", required = true, example = "MEMBER") @PathVariable Role oldRoleName,
            @RequestHeader("X-Operation-Id") UUID operationId) {

        boardAccessService.changeRoleBoardAccess(boardId, memberUserId, roleName, oldRoleName, operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Desactiva un permiso de acceso al tablero", description = "Modifica el campo active de la copia local de los datos de acceso")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se ha desactivado el permiso de acceso"),

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
    @PatchMapping("/deactivate/board/{boardId}/user/{memberUserId}/role/{roleName}/old-role/{oldRoleName}")
    public ResponseEntity<Void> deactivateBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId,
            @Parameter(description = "Rol nuevo del miembro", required = true, example = "ADMIN") @PathVariable Role roleName,
            @Parameter(description = "Rol antiguo del miembro", required = true, example = "MEMBER") @PathVariable Role oldRoleName,
            @RequestHeader("X-Operation-Id") UUID operationId) {
        boardAccessService.deactivateBoardAccess(boardId, memberUserId, roleName, oldRoleName, operationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}
