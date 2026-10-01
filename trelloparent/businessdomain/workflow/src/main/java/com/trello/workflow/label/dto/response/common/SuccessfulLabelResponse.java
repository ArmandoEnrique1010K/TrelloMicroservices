package com.trello.workflow.label.dto.response.common;

import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.label.dto.response.LabelResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulLabelResponse", description = "Respuesta exitosa al obtener una etiqueta")
public class SuccessfulLabelResponse extends SuccessfulResponse<LabelResponse> {

}
