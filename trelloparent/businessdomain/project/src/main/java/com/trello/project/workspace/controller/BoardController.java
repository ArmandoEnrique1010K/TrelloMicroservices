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
import com.trello.project.common.SuccessfulListResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.security.JwtUtils;
import com.trello.project.workspace.dto.request.BoardRequest;
import com.trello.project.workspace.dto.request.WorkspaceRequest;
import com.trello.project.workspace.dto.response.BoardResponse;
import com.trello.project.workspace.dto.response.WorkspaceResponse;
import com.trello.project.workspace.dto.response.common.SuccessfulWorkspaceListResponse;
import com.trello.project.workspace.dto.response.common.SuccessfulWorkspaceResponse;
import com.trello.project.workspace.exception.BoardAlreadyExistsException;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;
import com.trello.project.workspace.service.BoardService;
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

@Tag(name = "Board API", description = "API para la gestión de tableros por el usuario autenticado")
@RestController
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Operation(summary = "Agrega un tablero", description = "Agrega un tablero al espacio de trabajo por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha creado el espacio de trabajo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulWorkspaceResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "createdAt": "2026-09-03T21:47:52.333057",
                            "description": "Descripción de prueba para el tablero",
                            "id": "09901933-c12c-469f-bbfe-b51840f79d15",
                            "name": "Tablero de prueba"
                        },
                        "message": "Se agrego un nuevo tablero"
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
            @ApiResponse(responseCode = "409", description = "El nombre del tablero existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A board with the provided name already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Existe un tablero con ese nombre",
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
    @PostMapping("/workspace/{workspaceId}")
    public ResponseEntity<SuccessfulResponse<BoardResponse>> createBoard(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del espacio de trabajo", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID workspaceId,
            @Valid @RequestBody BoardRequest input) throws BoardAlreadyExistsException {

        UUID userId = JwtUtils.getUserId(jwt);

        BoardResponse response = boardService.createBoardByWorkspaceId(workspaceId, userId, input);

        SuccessfulResponse<BoardResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se agrego un nuevo tablero");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Lista los tableros", description = "Obtiene una lista de los tableros por Id de espacio de trabajo y usuario autenticado desde la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de tableros por el Id del espacio de trabajo", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BoardResponse.class)))),
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
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<BoardResponse>> listAllBoardsByWorkspaceId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del espacio de trabajo", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID workspaceId) {
        UUID userId = JwtUtils.getUserId(jwt);
        List<BoardResponse> response = boardService.listAllBoardsByWorkspaceId(workspaceId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
// @ApiResponse(responseCode = "200", description = "Obtiene la lista de
// tableros del espacio de trabajo", content = @Content(mediaType =
// "application/json", array = @ArraySchema(schema = @Schema(implementation =
// BoardResponse.class)))),
