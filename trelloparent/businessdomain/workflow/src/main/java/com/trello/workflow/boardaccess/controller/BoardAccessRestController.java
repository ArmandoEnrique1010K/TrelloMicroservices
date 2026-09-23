package com.trello.workflow.boardaccess.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

            // TODO: AÑADIR MENSAJE DE ERROR DE CUANDO EL PERMISO DE ACCESO YA SE ENCUENTRA
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
    @PostMapping("/board/{boardId}/role/{roleName}")
    public ResponseEntity<Void> saveBoardAccess(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Parameter(description = "Rol del miembro", required = true, example = "OWNER") @PathVariable Role roleName) {

        UUID userId = JwtUtils.getUserId(jwt);

        boardAccessService.saveBoardAccess(boardId, userId, roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

    }

}
