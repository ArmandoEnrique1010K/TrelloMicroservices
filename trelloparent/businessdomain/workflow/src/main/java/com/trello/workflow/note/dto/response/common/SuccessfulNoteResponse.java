package com.trello.workflow.note.dto.response.common;

import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.note.dto.response.NoteResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulNoteResponse", description = "Respuesta exitosa al obtener una nota")
public class SuccessfulNoteResponse extends SuccessfulResponse<NoteResponse> {

}
