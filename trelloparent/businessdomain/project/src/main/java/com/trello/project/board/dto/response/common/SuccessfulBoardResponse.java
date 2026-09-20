package com.trello.project.board.dto.response.common;

import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulBoardResponse", description = "Respuesta exitosa al obtener un tablero")
public class SuccessfulBoardResponse extends SuccessfulResponse<BoardResponse> {

}
