package com.trello.project.invitation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.common.SuccessfulVoidResponse;
import com.trello.project.invitation.dto.response.ReceivedInvitationResponse;
import com.trello.project.invitation.service.InvitationService;
import com.trello.project.security.JwtUtils;

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

@Tag(name = "Invitation Recipient API", description = "API para la gestión de invitaciones enviadas al usuario autenticado")
@RestController
@RequestMapping("/invitation")
public class InvitationRecipientRestController {

    private final InvitationService invitationService;

    public InvitationRecipientRestController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(summary = "Lista las invitaciones recibidas", description = "Obtiene una lista de las invitaciones que fuerón enviadas al usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de invitaciones", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReceivedInvitationResponse.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "boardName": "Primer tablero editado",
                            "id": "a633a216-e0d3-438f-9928-cf0a25ff63ad",
                            "message": "Unete al tablero como observador",
                            "role": "VIEWER",
                            "sendedAt": "2026-09-12T21:27:09.547918",
                            "senderUser": {
                                "email": "example@gmail.com",
                                "firstName": "Jhon",
                                "id": "3e54e44f-8f87-445b-8817-8c13010f5da5",
                                "lastName": "Doe"
                            },
                            "workspaceName": "Proyecto de prueba"
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
    @GetMapping
    public ResponseEntity<List<ReceivedInvitationResponse>> listAllInvitationsByRecipientUserId(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<ReceivedInvitationResponse> response = invitationService.listAllInvitationsByRecipientUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Aceptar invitación por el usuario receptor
    @Operation(summary = "Acepta una invitación", description = "Acepta una invitación y agrega al usuario receptor como miembro del tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aceptastes la invitación", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Aceptastes la invitación"
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
    @PutMapping("/accept/{invitationId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> acceptInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la invitación", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("invitationId") UUID invitationId) {

        UUID userId = JwtUtils.getUserId(jwt);
        invitationService.acceptInvitation(invitationId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Aceptastes la invitación");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Rechaza una invitación", description = "Rechaza una invitación")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitación rechazada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Invitación rechazada"
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
    @PatchMapping("/decline/{invitationId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> declineInvitation(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la invitación", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("invitationId") UUID invitationId) {

        UUID userId = JwtUtils.getUserId(jwt);
        invitationService.declineInvitation(invitationId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Invitación rechazada");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

}
