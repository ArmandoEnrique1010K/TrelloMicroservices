package com.trello.project.workspace.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Operation;
import com.trello.project.entities.Workspace;
import com.trello.project.enums.OperationStatus;
import com.trello.project.operation.service.handler.OperationDeleteWorkspaceService;
import com.trello.project.operation.service.processor.OperationDeleteWorkspaceProcessor;
import com.trello.project.operation.service.register.OperationRegisterService;
import com.trello.project.service.WorkspaceProjectService;
import com.trello.project.workspace.dto.request.WorkspaceRequest;
import com.trello.project.workspace.dto.response.WorkspaceResponse;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;
import com.trello.project.workspace.mapper.WorkspaceRequestMapper;
import com.trello.project.workspace.mapper.WorkspaceResponseMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceProjectService workspaceProjectService;
    private final WorkspaceRequestMapper workspaceRequestMapper;
    private final WorkspaceResponseMapper workspaceResponseMapper;
    private final OperationRegisterService operationRegisterService;
    private final OperationDeleteWorkspaceService operationDeleteWorkspaceService;
    private final OperationDeleteWorkspaceProcessor operationDeleteWorkspaceProcessor;

    public WorkspaceServiceImpl(WorkspaceProjectService workspaceProjectService,
            WorkspaceRequestMapper workspaceRequestMapper, WorkspaceResponseMapper workspaceResponseMapper,
            OperationRegisterService operationRegisterService,
            OperationDeleteWorkspaceService operationDeleteWorkspaceService,
            OperationDeleteWorkspaceProcessor operationDeleteWorkspaceProcessor) {
        this.workspaceProjectService = workspaceProjectService;
        this.workspaceRequestMapper = workspaceRequestMapper;
        this.workspaceResponseMapper = workspaceResponseMapper;
        this.operationRegisterService = operationRegisterService;
        this.operationDeleteWorkspaceService = operationDeleteWorkspaceService;
        this.operationDeleteWorkspaceProcessor = operationDeleteWorkspaceProcessor;
    }

    @Override
    public WorkspaceResponse createWorkspace(UUID ownerUserId, WorkspaceRequest workspaceRequest)
            throws WorkspaceAlreadyExistsException {
        String name = workspaceRequest.getName();

        if (workspaceProjectService.existsWorkspaceByOwnerIdAndName(ownerUserId, name)) {
            throw new WorkspaceAlreadyExistsException();
        }

        Workspace workspaceToWorkspaceRequest = workspaceRequestMapper.workspaceRequestToWorkspace(workspaceRequest);
        workspaceToWorkspaceRequest.setOwnerUserId(ownerUserId);
        workspaceToWorkspaceRequest.setStatus(true);

        Workspace savedWorkspace = workspaceProjectService.saveWorkspace(workspaceToWorkspaceRequest);
        WorkspaceResponse workspaceResponse = workspaceResponseMapper.workspaceToWorkspaceResponse(savedWorkspace);

        return workspaceResponse;
    }

    @Override
    public List<WorkspaceResponse> listAllWorkspaces(UUID ownerUserId) {
        List<Workspace> listWorkspace = workspaceProjectService.findAllWorkspaceByOwnerId(ownerUserId);
        return workspaceResponseMapper.workspaceListToWorkspaceResponseList(listWorkspace);
    }

    @Override
    public WorkspaceResponse editWorkspace(UUID ownerUserId, UUID workspaceId,
            WorkspaceRequest workspaceRequest)
            throws WorkspaceAlreadyExistsException {
        String name = workspaceRequest.getName();
        String description = workspaceRequest.getDescription();

        Workspace findedWorkspace = workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(workspaceId,
                ownerUserId);

        if (workspaceProjectService.existsWorkspaceByOwnerIdAndNameExcludingId(ownerUserId, name,
                workspaceId)) {
            throw new WorkspaceAlreadyExistsException();
        }

        findedWorkspace.setName(name);
        findedWorkspace.setDescription(description);

        Workspace savedWorkspace = workspaceProjectService.saveWorkspace(findedWorkspace);
        WorkspaceResponse workspaceResponse = workspaceResponseMapper.workspaceToWorkspaceResponse(savedWorkspace);

        return workspaceResponse;
    }

    @Override
    public void deleteWorkspace(UUID ownerUserId, UUID workspaceId) {

        // Primero se obtiene el Workspace para recuperar los IDs de los Boards que
        // posteriormente deben eliminarse en el microservicio Workflow
        // Workspace findedWorkspace =
        // workspaceProjectService.findWorkspaceByIdAndOwnerUserId(workspaceId,
        // ownerUserId);
        // List<UUID> boardsIds = findedWorkspace.getBoards()
        // .stream()
        // .map(board -> board.getId())
        // .toList();

        // Elimina el Workspace y sus Boards pertenecientes al dominio del microservicio
        // Project
        // workspaceProjectService.deleteWorkspaceByIdAndOwnerUserId(workspaceId,
        // ownerUserId);

        // Workflow es responsable de eliminar los recursos
        // pertenecientes a sus propios dominios:
        //
        // Board
        // BoardAccess
        // Task
        //
        // Solamente se envían los IDs, no las entidades completas
        // workflowClientService.deleteManyBoardsByIdsAndBoardAccessAndTasks(boardsIds);

        Operation operation = operationRegisterService.registerDeleteWorkspace(ownerUserId, workspaceId);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
            operationDeleteWorkspaceService.applyDeleteWorkspace(operation.getId(), false);
        }

        operationDeleteWorkspaceProcessor.processDeleteWorkspaceOperation(operation.getId());
    }

}
