package com.trello.workflow.note.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.workflow.common.StandarizedApiExceptionResponse;
import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.common.SuccessfulVoidResponse;
import com.trello.workflow.note.dto.request.NoteRequest;
import com.trello.workflow.note.dto.response.NoteResponse;
import com.trello.workflow.note.dto.response.UserNoteResponse;
import com.trello.workflow.note.dto.response.common.SuccessfulNoteResponse;
import com.trello.workflow.note.service.NoteService;
import com.trello.workflow.security.JwtUtils;

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

@Tag(name = "Note API", description = "API para la gestión de notas")
@RestController
@RequestMapping("/note")
public class NoteRestController {

    private final NoteService noteService;

    public NoteRestController(NoteService noteService) {
        this.noteService = noteService;
    }

    // TODO: PROBAR LOS ENDPOINTS
    @Operation(summary = "Crea una nota", description = "Agrega una nota a la tarea por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Se ha creado la nota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulNoteResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6",
                            "content": "Contenido de prueba",
                            "createdAt": "2026-09-27T14:54:38.5068766"
                        },
                        "message": "Se ha creado la nota"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "content": "El contenido es obligatorio"
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
    @PostMapping("/task/{taskId}")
    public ResponseEntity<SuccessfulResponse<NoteResponse>> createNote(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID taskId,
            @Valid @RequestBody NoteRequest input) {
        UUID userId = JwtUtils.getUserId(jwt);

        NoteResponse response = noteService.createNote(input, taskId, userId);

        SuccessfulResponse<NoteResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha creado la nota");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    // TODO: INVESTIGAR SI ES NECESARIO HACER VARIAS PETICIONES POR SEPARADO O UNA
    // SOLA CUANDO SE TRATA DE OBTENER DETALLES DE UNA TAREA
    @Operation(summary = "Lista las notas", description = "Obtiene una lista de notas por ID de tarea en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de notas por ID de tarea", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserNoteResponse.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "id": "e6ee693c-9a84-4154-9dd3-52884dd882e6",
                            "content": "Contenido de prueba",
                            "createdAt": "2026-09-27T14:54:38.5068766",
                            "createdByUser": {
                                "email": "example@gmail.com",
                                "firstName": "Jhon",
                                "id": "3e54e44f-8f87-445b-8817-8c13010f5da5",
                                "lastName": "Doe"
                            }
                        }
                    ]
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
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<UserNoteResponse>> listAllNotesByTaskId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID taskId) {
        UUID userId = JwtUtils.getUserId(jwt);
        List<UserNoteResponse> response = noteService.listAllNotesByTaskId(taskId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Elimina una nota", description = "Elimina una nota por ID en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado la nota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado la nota"
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
                    @ExampleObject(name = "La nota no existe", summary = "La nota no existe", value = """
                            {
                                "detail": "The note was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado la nota",
                                "status": 404,
                                "title": "Note not found",
                                "type": "/errors/note-not-found"
                            }
                            """), })),
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

    @DeleteMapping("/{noteId}")
    public ResponseEntity<SuccessfulResponse<Void>> deleteNote(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la nota", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID noteId) {
        UUID userId = JwtUtils.getUserId(jwt);
        noteService.deleteNote(noteId, userId);

        SuccessfulResponse<Void> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado la nota");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}
