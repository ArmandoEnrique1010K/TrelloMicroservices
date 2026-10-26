package com.trello.project.member.dto.response.common;

import com.trello.project.common.SuccessfulResponse;
import com.trello.project.member.dto.response.MemberResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SucessfulMemberResponse", description = "Respuesta exitosa al obtener un miembro")
public class SucessfulMemberResponse extends SuccessfulResponse<MemberResponse> {

}
