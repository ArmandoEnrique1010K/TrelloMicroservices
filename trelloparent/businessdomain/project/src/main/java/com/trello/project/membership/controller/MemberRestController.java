package com.trello.project.membership.controller;

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
import com.trello.project.enums.Role;
import com.trello.project.membership.dto.response.MemberResponse;
import com.trello.project.membership.dto.response.ReceivedInvitationResponse;
import com.trello.project.membership.dto.response.UserMemberResponse;
import com.trello.project.membership.dto.response.common.SuccessfulInvitationResponse;
import com.trello.project.membership.service.MemberService;
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
@RequestMapping("/member")
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
            @ApiResponse(responseCode = "200", description = "Se ha cambiado el rol del miembro", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulInvitationResponse.class), examples = @ExampleObject(value = """
                    {
                      "body": {
                        "active": true,
                        "id": "95c91a33-967b-46b8-bf8e-14dd29fa87cc",
                        "role": "MEMBER"
                      },
                      "message": "Se ha cambiado el rol del miembro"
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
                        "detail": "The member was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el miembro",
                        "status": 404,
                        "title": "Member not found",
                        "type": "/errors/member-not-found"
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
            @ApiResponse(responseCode = "200", description = "Se ha eliminado un miembro", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulInvitationResponse.class), examples = @ExampleObject(value = """
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
            @ApiResponse(responseCode = "404", description = "No se ha encontrado el miembro", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandarizedApiExceptionResponse.class), examples = @ExampleObject(value = """
                    {
                        "detail": "The member was not found in the system",
                        "fields": null,
                        "instance": null,
                        "message": "No se ha encontrado el miembro",
                        "status": 404,
                        "title": "Member not found",
                        "type": "/errors/member-not-found"
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
    @PatchMapping("/{memberId}")
    public ResponseEntity<SuccessfulResponse<MemberResponse>> deactivateMember(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del miembro", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable("memberId") UUID memberId) {
        UUID userId = JwtUtils.getUserId(jwt);
        memberService.deactivateMember(memberId, userId);

        SuccessfulResponse<MemberResponse> successfulResponse = new SuccessfulResponse<>();
        successfulResponse.setMessage("Se ha eliminado un miembro");
        successfulResponse.setBody(null);

        return ResponseEntity.status(HttpStatus.OK).body(successfulResponse);

    }
}
