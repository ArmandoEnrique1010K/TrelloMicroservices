package com.trello.workflow.boardaccess.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

@Tag(name = "Board Access API", description = "API para la gestión de permisos de acceso al tablero por usuarios")
@RestController
@RequestMapping("/boardAccess")
public class BoardAccessRestController {

    private final BoardAccessService boardAccessService;

    public BoardAccessRestController(BoardAccessService boardAccessService) {
        this.boardAccessService = boardAccessService;
    }

    @Operation(summary = "Guarda los permisos de acceso", description = "Crea una copia local de los datos de acceso")
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

            @ApiResponse(responseCode = "409", description = "El permiso de acceso al tablero existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A board access with the provided board ID and user ID already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Ya existe un permiso de acceso al tablero",
                        "status": 409,
                        "title": "Board access already exists",
                        "type": "/errors/board-access/already-exists"
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
    @PostMapping("/board/{boardId}/role/{roleName}")
    public ResponseEntity<Void> addBoardAccess(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "Rol del miembro", required = true, example = "OWNER") @PathVariable Role roleName) {

        UUID userId = JwtUtils.getUserId(jwt);

        boardAccessService.addBoardAccess(boardId, userId, roleName);
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
    @PutMapping("/board/{boardId}/user/{memberUserId}/role/{roleName}")
    // El ID del usuario se obtiene desde un parametro
    public ResponseEntity<Void> changeRoleBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId,
            @Parameter(description = "Rol del miembro", required = true, example = "OWNER") @PathVariable Role roleName) {

        boardAccessService.changeRoleBoardAccess(boardId, memberUserId, roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Desactiva un permiso de acceso", description = "Modifica el campo active de la copia local de los datos de acceso")
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
    @PatchMapping("/board/{boardId}/user/{memberUserId}")
    public ResponseEntity<Void> deactivateBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId) {
        boardAccessService.deactivateBoardAccess(boardId, memberUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Activa un permiso de acceso", description = "Modifica el campo active de la copia local de los datos de acceso")
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

    @PatchMapping("/board/{boardId}/user/{memberUserId}/role/{roleName}")
    public ResponseEntity<Void> activateBoardAccess(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "ID del usuario", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID memberUserId,
            @Parameter(description = "Rol del miembro", required = true, example = "OWNER") @PathVariable Role roleName) {
        boardAccessService.activateBoardAccess(boardId, memberUserId, roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Elimina todos los permisos de acceso por ID de tablero", description = "Elimina del sistema todos los permisos de acceso por ID de tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Se han eliminado todos los permisos de acceso"),

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
    @DeleteMapping("/board/{boardId}")
    public ResponseEntity<Void> deleteAllBoardAccessByBoardId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);
        boardAccessService.deleteAllBoardAccessByBoardId(boardId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
