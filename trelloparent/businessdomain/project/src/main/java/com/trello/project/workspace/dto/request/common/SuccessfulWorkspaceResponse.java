package com.trello.project.workspace.dto.request.common;

import com.trello.project.common.SuccessfulResponse;
import com.trello.project.workspace.dto.response.WorkspaceResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulWorkspaceResponse", description = "Respuesta exitosa al crear un espacio de trabajo")
public class SuccessfulWorkspaceResponse extends SuccessfulResponse<WorkspaceResponse> {

}
