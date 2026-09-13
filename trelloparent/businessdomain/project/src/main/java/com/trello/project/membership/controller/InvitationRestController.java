package com.trello.project.membership.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.service.InvitationService;
import com.trello.project.security.JwtUtils;
import com.trello.project.workspace.dto.response.common.SuccessfulBoardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Invitation API", description = "API para la gestión de invitaciones de tableros hacia los usuarios")
@RestController
@RequestMapping("/invitation")
public class InvitationRestController {

    private final InvitationService invitationService;

    public InvitationRestController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(summary = "Envia una invitación", description = "Crea una invitación en la base de datos")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Se ha enviado la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulBoardResponse.class), examples = @ExampleObject(value = """
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

            //
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
            //
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
}
