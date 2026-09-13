package com.trello.project.workspace.dto.response.common;

import com.trello.project.common.SuccessfulResponse;
import com.trello.project.workspace.dto.response.BoardResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulBoardResponse", description = "Respuesta exitosa al obtener un tablero")
public class SuccessfulBoardResponse extends SuccessfulResponse<BoardResponse> {

}
