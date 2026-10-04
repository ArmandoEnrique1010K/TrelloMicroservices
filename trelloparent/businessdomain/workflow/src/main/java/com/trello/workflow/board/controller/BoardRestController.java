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

            @ApiResponse(responseCode = "409", description = "El tablero existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A board with the provided ID already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Ya existe el tablero",
                        "status": 409,
                        "title": "Board already exists",
                        "type": "/errors/board/already-exists"
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
    @PostMapping("/{boardId}")
    public ResponseEntity<Void> addBoardAndBoardAccessUserOwner(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {

        UUID userId = JwtUtils.getUserId(jwt);

        boardService.addBoardAndBoardAccessUserOwner(boardId, userId);
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

            @ApiResponse(responseCode = "404", description = "El tablero no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The board was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el tablero",
                        "status": 404,
                        "title": "Board not found",
                        "type": "/errors/board-not-found"
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
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoardByIdAndBoardAccessAndTasks(
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {

        boardService.deleteBoardByIdAndBoardAccessAndTasks(boardId);
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

            // TODO: AÑADIR IDEMPOTENCIA
            // @ApiResponse(responseCode = "404", description = "El espacio de trabajo no
            // existe", content = @Content(mediaType = "application/json", schema =
            // @Schema(implementation = StandarizedApiExceptionResponse.class), examples =
            // @ExampleObject(value = """
            // {
            // "detail": "The board was not found in the system",
            // "fields": null,
            // "instance": null,
            // "message": "No se ha encontrado el tablero",
            // "status": 404,
            // "title": "Board not found",
            // "type": "/errors/board-not-found"
            // }
            // """))),

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
            @Parameter(description = "Lista de IDs del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @RequestParam(required = false) List<UUID> boardIds) {
        boardService.deleteAllBoardsByIdsAndBoardAccessAndTasks(boardIds);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
