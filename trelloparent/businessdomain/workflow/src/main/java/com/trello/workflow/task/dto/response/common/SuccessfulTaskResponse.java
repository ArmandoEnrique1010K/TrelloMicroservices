package com.trello.workflow.task.dto.response.common;

import com.trello.workflow.common.SuccessfulResponse;
import com.trello.workflow.task.dto.response.TaskResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulTaskResponse", description = "Respuesta exitosa al obtener una tarea")
public class SuccessfulTaskResponse extends SuccessfulResponse<TaskResponse> {

}
