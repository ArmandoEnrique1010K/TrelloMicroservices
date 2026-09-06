package com.trello.project.workspace.dto.response.common;

import com.trello.project.common.SuccessfulListResponse;
import com.trello.project.workspace.dto.response.WorkspaceResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulWorkspaceListResponse", description = "Respuesta exitosa al listar espacios de trabajo")
public class SuccessfulWorkspaceListResponse extends SuccessfulListResponse<WorkspaceResponse> {

}
