package com.trello.project.invitation.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.common.SuccessfulVoidResponse;
import com.trello.project.invitation.dto.request.InvitationRequest;
import com.trello.project.invitation.dto.response.BoardInvitationResponse;
import com.trello.project.invitation.dto.response.InvitationResponse;
import com.trello.project.invitation.dto.response.common.SuccessfulInvitationResponse;
import com.trello.project.invitation.service.InvitationService;
import com.trello.project.security.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

// ¡Este error me retraso 1 dia de trabajo!
// import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Invitation Admin API", description = "API para la gestión de invitaciones desde el administrador del espacio de trabajo")
@RestController
@RequestMapping("/invitation")
public class InvitationAdminRestController {

    private final InvitationService invitationService;

    public InvitationAdminRestController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(summary = "Envia una invitación", description = "Crea una invitación en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Se ha enviado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulInvitationResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "createdAt": "2026-09-12T21:36:30.577825",
                            "id": "e2bd7845-a8c2-4904-bf8f-c394fa00e6f4",
                            "message": "Unete al tablero como miembro",
                            "recipientUserId": "858d7655-6178-458f-b880-3db6aa3beff3",
                            "role": "ADMIN",
                            "senderUserId": "af6cf6ea-05c3-4233-a546-f942386f43ad"
                        },
                        "message": "Se ha enviado la invitación"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "role": "El rol es obligatorio"
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
            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "No se ha encontrado el tablero", summary = "No se ha encontrado el tablero", value = """
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
                    @ExampleObject(name = "No se ha encontrado el espacio de trabajo", summary = "No se ha encontrado el espacio de trabajo", value = """
                            {
                                "detail": "The workspace was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el espacio de trabajo",
                                "status": 404,
                                "title": "Workspace not found",
                                "type": "/errors/workspace-not-found"
                            }
                            """)
            })),
            @ApiResponse(responseCode = "409", description = "La invitación existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "An invitation to the user from this board already exists",
                        "fields": null,
                        "instance": null,
                        "message": "Existe una invitación al usuario que proviene de este tablero",
                        "status": 409,
                        "title": "Invitation already exists",
                        "type": "/errors/invitation/already-exists"
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
    @PostMapping("/send/{recipientUserId}/board/{boardId}")
    public ResponseEntity<SuccessfulResponse<InvitationResponse>> sendInvitation(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del usuario receptor", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("recipientUserId") UUID recipientUserId,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("boardId") UUID boardId,
            @Valid @RequestBody InvitationRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);

        InvitationResponse response = invitationService.sendInvitation(boardId, userId, recipientUserId, input);

        SuccessfulResponse<InvitationResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha enviado la invitación");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(successfulResponse);
    }

    @Operation(summary = "Lista las invitaciones emitidas en un tablero", description = "Obtiene una lista de las invitaciones de un tablero que fueron enviadas")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de invitaciones por el ID del tablero", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BoardInvitationResponse.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "id": "e2bd7845-a8c2-4904-bf8f-c394fa00e6f4",
                            "message": "Unete al tablero como miembro",
                            "recipientUser": {
                                "email": "cmclese1@canalblog.com",
                                "firstName": "dmclese1",
                                "id": "858d7655-6178-458f-b880-3db6aa3beff3",
                                "lastName": "McLese"
                            },
                            "role": "ADMIN",
                            "sendedAt": "2026-09-12T21:36:30.577825",
                            "status": "UNCONFIRMED"
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
            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "No se ha encontrado el tablero", summary = "No se ha encontrado el tablero", value = """
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
                    @ExampleObject(name = "No se ha encontrado el espacio de trabajo", summary = "No se ha encontrado el espacio de trabajo", value = """
                            {
                                "detail": "The workspace was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el espacio de trabajo",
                                "status": 404,
                                "title": "Workspace not found",
                                "type": "/errors/workspace-not-found"
                            }
                            """)
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
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<BoardInvitationResponse>> listAllInvitationsByBoardId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("boardId") UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<BoardInvitationResponse> response = invitationService.listAllInvitationsByBoardId(boardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Edita una invitación enviada", description = "Modifica el mensaje y/o el rol de una invitación")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha editado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulInvitationResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "createdAt": "2026-09-12T21:36:30.577825",
                            "id": "e2bd7845-a8c2-4904-bf8f-c394fa00e6f4",
                            "message": "Unete al tablero como observador",
                            "recipientUserId": "858d7655-6178-458f-b880-3db6aa3beff3",
                            "role": "VIEWER",
                            "senderUserId": "af6cf6ea-05c3-4233-a546-f942386f43ad"
                        },
                        "message": "Se ha editado la invitación"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "One or more request fields are invalid",
                        "fields": {
                            "role": "El rol es obligatorio"
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
            @ApiResponse(responseCode = "404", description = "No se ha encontrado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The invitation was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado la invitación",
                        "status": 404,
                        "title": "Invitation not found",
                        "type": "/errors/invitation-not-found"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "La invitación ya ha sido confirmada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The invitation has already been confirmed",
                        "fields": null,
                        "instance": null,
                        "message": "La invitación ya ha sido confirmada",
                        "status": 404,
                        "title": "Invitation confirmed",
                        "type": "/errors/invitation-confirmed"
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
    @PutMapping("/{invitationId}")
    public ResponseEntity<SuccessfulResponse<InvitationResponse>> editInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la invitación", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("invitationId") UUID invitationId,
            @Valid @RequestBody InvitationRequest input) {

        UUID userId = JwtUtils.getUserId(jwt);
        InvitationResponse response = invitationService.editInvitation(invitationId, input, userId);

        SuccessfulResponse<InvitationResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha editado la invitación");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);

    }

    @Operation(summary = "Elimina una invitación", description = "Elimina una invitación en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado la invitación"
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
            @ApiResponse(responseCode = "404", description = "No se ha encontrado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The invitation was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado la invitación",
                        "status": 404,
                        "title": "Invitation not found",
                        "type": "/errors/invitation-not-found"
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
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> deleteInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la invitación", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("invitationId") UUID invitationId) {
        UUID userId = JwtUtils.getUserId(jwt);
        invitationService.deleteInvitation(invitationId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado la invitación");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Busca usuarios disponibles", description = "Busca usuarios disponibles en el sistema para ser invitados")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios disponibles encontrados", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "email": "opiddington8@moonfruit.com",
                            "firstName": "spiddington8",
                            "id": "cbdd2b41-09b4-4e9b-adeb-79ce7dc0abb0",
                            "lastName": "Piddington"
                        },
                        {
                            "email": "opiddington9@desdev.cn",
                            "firstName": "ygully3",
                            "id": "26b430c5-37ef-499d-9a31-7369cc7a572f",
                            "lastName": "Gully"
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
    @GetMapping("/search/availableUsers/board/{boardId}")
    public ResponseEntity<List<UserResponse>> searchAvailableUsers(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("boardId") UUID boardId,
            // Recordar que el correo es obligatorio y debe tener minimo 6 caracteres
            @Parameter(description = "Email del usuario", required = false, example = "enrique@gmail.com") @RequestParam(value = "email", required = false) String email) {

        UUID userId = JwtUtils.getUserId(jwt);
        List<UserResponse> availableUsers = invitationService.listAllAvailableUsers(boardId, userId, email);

        return ResponseEntity.status(HttpStatus.OK).body(availableUsers);
    }

}
