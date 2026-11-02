package com.trello.project.invitation.dto.response.common;

import com.trello.project.client.dto.response.UsersListResponse;
import com.trello.project.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulUsersListResponse", description = "Respuesta exitosa al obtener la lista de usuarios")
public class SuccessfulUsersListResponse extends SuccessfulResponse<UsersListResponse> {

}
