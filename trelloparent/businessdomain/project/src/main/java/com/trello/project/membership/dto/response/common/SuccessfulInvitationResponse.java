package com.trello.project.membership.dto.response.common;

import com.trello.project.common.SuccessfulResponse;
import com.trello.project.membership.dto.response.InvitationResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulInvitationResponse", description = "Respuesta exitosa al obtener un tablero")
public class SuccessfulInvitationResponse extends SuccessfulResponse<InvitationResponse> {

}
