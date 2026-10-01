package com.trello.workflow.label.controller;

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

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.common.SuccessfulVoidResponse;
import com.trello.workflow.label.dto.request.LabelRequest;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.label.dto.response.common.SuccessfulLabelResponse;
import com.trello.workflow.label.service.LabelService;
import com.trello.workflow.security.JwtUtils;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;

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

// TODO: PROBAR LOS ENDPOINTS
@Tag(name = "Label API", description = "API para la gestión de etiquetas")
@RestController
@RequestMapping("/label")
public class LabelRestController {

    private final LabelService labelService;

    public LabelRestController(LabelService labelService) {
        this.labelService = labelService;
    }

    @Operation(summary = "Agrega una etiqueta", description = "Agrega una etiqueta al tablero por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha creado la etiqueta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulLabelResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "color": "RED",
                            "content": "Importante",
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6"
                        },
                        "message": "Se ha creado la etiqueta"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "content": "El contenido debe tener entre 4 y 50 caracteres"
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
            @ApiResponse(responseCode = "409", description = "La etiqueta existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A label with the provided content already exists in this board",
                        "fields": null,
                        "instance": null,
                        "message": "Ya existe una etiqueta con ese contenido",
                        "status": 409,
                        "title": "Label already exists",
                        "type": "/errors/label/already-exists"
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
    public ResponseEntity<SuccessfulResponse<LabelResponse>> createLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId,
            @Valid @RequestBody LabelRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);

        LabelResponse response = labelService.createLabel(input, boardId, userId);
        SuccessfulResponse<LabelResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha creado la etiqueta");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Lista las etiquetas", description = "Obtiene una lista de etiquetas por ID del tablero en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de tareas por ID de tablero", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuthorTaskResponse.class)))),

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
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<LabelResponse>> listAllLabels(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<LabelResponse> response = labelService.listAllLabels(boardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Edita una etiqueta", description = "Edita los datos de una etiqueta por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha modificado la etiqueta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulLabelResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "color": "RED",
                            "content": "Importante",
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6"
                        },
                        "message": "Se ha modificado la etiqueta"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "content": "El contenido debe tener entre 4 y 50 caracteres"
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
                    @ExampleObject(name = "La etiqueta no existe", summary = "La etiqueta no existe", value = """
                            {
                                "detail": "The label was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la etiqueta",
                                "status": 404,
                                "title": "Label not found",
                                "type": "/errors/label-not-found"
                            }
                            """),
            })),
            @ApiResponse(responseCode = "409", description = "La etiqueta existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "A label with the provided content already exists in this board",
                        "fields": null,
                        "instance": null,
                        "message": "Ya existe una etiqueta con ese contenido",
                        "status": 409,
                        "title": "Label already exists",
                        "type": "/errors/label/already-exists"
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
    @PutMapping("/{labelId}")
    public ResponseEntity<SuccessfulResponse<LabelResponse>> editLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de etiqueta", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID labelId,
            @Valid @RequestBody LabelRequest input) {
        UUID userId = JwtUtils.getUserId(jwt);
        LabelResponse response = labelService.editLabel(labelId, input, userId);

        SuccessfulResponse<LabelResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha modificado la etiqueta");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Elimina una etiqueta", description = "Elimina una etiqueta por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado la tarea", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado la etiqueta"
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
                    @ExampleObject(name = "La etiqueta no existe", summary = "La etiqueta no existe", value = """
                            {
                                "detail": "The label was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la etiqueta",
                                "status": 404,
                                "title": "Label not found",
                                "type": "/errors/label-not-found"
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
    @DeleteMapping("/{labelId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> deleteLabel(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de etiqueta", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID labelId) {
        UUID userId = JwtUtils.getUserId(jwt);
        labelService.deleteLabel(labelId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado la etiqueta");
        successfulResponse.setBody(null);
        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}
