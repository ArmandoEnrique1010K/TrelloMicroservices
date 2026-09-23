package com.trello.workflow.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulVoidResponse", description = "Respuesta exitosa sin un body")
public class SuccessfulVoidResponse extends SuccessfulResponse<Void> {

}