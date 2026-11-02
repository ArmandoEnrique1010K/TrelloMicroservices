package com.trello.project.member.controller;

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

import com.trello.project.board.dto.response.common.SuccessfulBoardResponse;
import com.trello.project.common.StandarizedApiExceptionResponse;
import com.trello.project.common.SuccessfulResponse;
import com.trello.project.common.SuccessfulVoidResponse;
import com.trello.project.enums.Role;
import com.trello.project.invitation.dto.response.ReceivedInvitationResponse;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.member.dto.response.UserMemberResponse;
import com.trello.project.member.dto.response.common.SucessfulMemberResponse;
import com.trello.project.member.service.MemberService;
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

@Tag(name = "Member API", description = "API para la gestión de miembros de tableros")
@RestController
@RequestMapping("/members")
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Lista los miembros de un tablero", description = "Obtiene una lista de los miembros de un tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene la lista de miembros, si se trata del usuario administrador del espacio de trabajo, incluye los usuarios que han sido borrado logicamente; de lo contrario no los incluye", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReceivedInvitationResponse.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "active": true,
                            "id": "95c91a33-967b-46b8-bf8e-14dd29fa87cc",
                            "joinedAt": "2026-09-13T16:35:50.452319",
                            "memberUser": {
                                "email": "example@gmail.com",
                                "firstName": "Jhon",
                                "id": "3e54e44f-8f87-445b-8817-8c13010f5da5",
                                "lastName": "Doe"
                            },
                            "role": "VIEWER"
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

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el tablero", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
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
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<UserMemberResponse>> listAllMembersByBoardId(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del tablero", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("boardId") UUID boardId) {
        UUID userId = JwtUtils.getUserId(jwt);

        List<UserMemberResponse> response = memberService.listAllMembersByBoardId(boardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Cambia el rol de un miembro", description = "Modifica el rol de un miembro de un tablero")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha cambiado el rol del miembro", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SucessfulMemberResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": {
                            "active": true,
                            "id": "95c91a33-967b-46b8-bf8e-14dd29fa87cc",
                            "role": "MEMBER"
                        },
                        "message": "Se ha cambiado el rol del miembro"
                    }
                    """))),

            @ApiResponse(responseCode = "202", description = "La operación quedo en un estado pendiente en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulBoardResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The operation has been left in a pending state because one of the services has gone down",
                        "fields": null,
                        "instance": null,
                        "message": "La operación quedo en un estado pendiente, vuelva a intentarlo en un momento",
                        "status": 202,
                        "title": "Operation compensated",
                        "type": "/errors/operation/compensated"
                    }
                    """))),

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "No se ha encontrado el miembro", summary = "No se ha encontrado el espacio de trabajo", value = """
                            {
                                "detail": "The member was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el miembro",
                                "status": 404,
                                "title": "Member not found",
                                "type": "/errors/member-not-found"
                            }
                            """),
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
                            """)
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El estado del miembro se encuentra inactivo", summary = "El estado del miembro se encuentra inactivo", value = """
                            {
                                "detail": "The member has been removed",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 409,
                                "title": "Member inactive",
                                "type": "/errors/member-inactive"
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
                    @ExampleObject(name = "Error de lógica de negocio", summary = "Error de lógica de negocio", value = """
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

            @ApiResponse(responseCode = "500", description = "Error relacionado al servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "Error interno del servidor", summary = "Error interno del servidor", value = """
                            {
                              "detail": "An unexpected error occurred while processing the request",
                              "fields": null,
                              "instance": null,
                              "message": "Ha ocurrido un error inesperado",
                              "status": 500,
                              "title": "Internal server error",
                              "type": "/errors/internal-server-error"
                            }
                            """),
                    @ExampleObject(name = "Se requiere de una revisión manual en la operación realizada", summary = "Se requiere de una revisión manual en la operación realizada", value = """
                            {
                              "detail": "The operation requires careful review by the app administrator; please get in touch by sending an email to example@gmail.com",
                              "fields": null,
                              "instance": null,
                              "message": "Ha ocurrido un error, pongase en contacto con el administrador",
                              "status": 500,
                              "title": "Operation manual review",
                              "type": "/errors/operation/manual-review"
                            }
                            """),
            })),
    })
    @PutMapping("/{memberId}/role/{roleName}")
    public ResponseEntity<SuccessfulResponse<MemberResponse>> changeRoleMemberById(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del miembro", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("memberId") UUID memberId,
            @Parameter(description = "Nombre del rol", required = true, example = "ADMIN") @PathVariable("roleName") Role roleName) {
        UUID userId = JwtUtils.getUserId(jwt);
        MemberResponse response = memberService.changeRoleMemberById(roleName, memberId, userId);

        SuccessfulResponse<MemberResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha cambiado el rol del miembro");
        successfulResponse.setBody(response);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }

    @Operation(summary = "Elimina un miembro", description = "Realiza un borrado lógico del miembro")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se ha eliminado un miembro", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulVoidResponse.class), examples = @ExampleObject(value = """
                    {
                        "body": null,
                        "message": "Se ha eliminado un miembro"
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
            @ApiResponse(responseCode = "202", description = "La operación quedo en un estado pendiente en el sistema", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulBoardResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The operation has been left in a pending state because one of the services has gone down",
                        "fields": null,
                        "instance": null,
                        "message": "La operación quedo en un estado pendiente, vuelva a intentarlo en un momento",
                        "status": 202,
                        "title": "Operation compensated",
                        "type": "/errors/operation/compensated"
                    }
                    """))),

            @ApiResponse(responseCode = "404", description = "No se ha encontrado el recurso solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "No se ha encontrado el miembro", summary = "No se ha encontrado el espacio de trabajo", value = """
                            {
                                "detail": "The member was not found in the system",
                                "fields": null,
                                "instance": null,
                                "message": "No se ha encontrado el miembro",
                                "status": 404,
                                "title": "Member not found",
                                "type": "/errors/member-not-found"
                            }
                            """),
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
                            """)
            })),

            @ApiResponse(responseCode = "409", description = "Error de lógica de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "El estado del miembro se encuentra inactivo", summary = "El estado del miembro se encuentra inactivo", value = """
                            {
                                "detail": "The member has been removed",
                                "fields": null,
                                "instance": null,
                                "message": "Ha ocurrido un error",
                                "status": 409,
                                "title": "Member inactive",
                                "type": "/errors/member-inactive"
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
                    @ExampleObject(name = "Error de lógica de negocio", summary = "Error de lógica de negocio", value = """
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

            @ApiResponse(responseCode = "500", description = "Error relacionado al servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = {
                    @ExampleObject(name = "Error interno del servidor", summary = "Error interno del servidor", value = """
                            {
                              "detail": "An unexpected error occurred while processing the request",
                              "fields": null,
                              "instance": null,
                              "message": "Ha ocurrido un error inesperado",
                              "status": 500,
                              "title": "Internal server error",
                              "type": "/errors/internal-server-error"
                            }
                            """),
                    @ExampleObject(name = "Se requiere de una revisión manual en la operación realizada", summary = "Se requiere de una revisión manual en la operación realizada", value = """
                            {
                              "detail": "The operation requires careful review by the app administrator; please get in touch by sending an email to example@gmail.com",
                              "fields": null,
                              "instance": null,
                              "message": "Ha ocurrido un error, pongase en contacto con el administrador",
                              "status": 500,
                              "title": "Operation manual review",
                              "type": "/errors/operation/manual-review"
                            }
                            """),
            })),
    })
    @PatchMapping("/{memberId}")
    public ResponseEntity<SuccessfulResponse<SuccessfulVoidResponse>> deactivateMember(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del miembro", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("memberId") UUID memberId) {
        UUID userId = JwtUtils.getUserId(jwt);
        memberService.deactivateMember(memberId, userId);

        SuccessfulResponse<SuccessfulVoidResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado un miembro");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);
    }
}
