package com.trello.project.invitation.dto.response.common;

import com.trello.project.common.SuccessfulResponse;
import com.trello.project.invitation.dto.response.InvitationResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulInvitationResponse", description = "Respuesta exitosa al obtener un tablero")
public class SuccessfulInvitationResponse extends SuccessfulResponse<InvitationResponse> {

}
