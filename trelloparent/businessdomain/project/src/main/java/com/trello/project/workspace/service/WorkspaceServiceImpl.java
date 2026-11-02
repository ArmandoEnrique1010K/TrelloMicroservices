package com.trello.project.workspace.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.entities.Workspace;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
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

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public WorkspaceResponse editWorkspace(UUID ownerUserId, UUID workspaceId,
            WorkspaceRequest workspaceRequest)
            throws WorkspaceNotFoundException, WorkspaceAlreadyExistsException {
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
    public void deleteWorkspace(UUID ownerUserId, UUID workspaceId)
            throws WorkspaceNotFoundException, OperationNotPendingException, OperationNotFoundException,
            OperationConflictException, OperationCompensatedException, BusinessRuleException,
            OperationManualReviewException {

        Operation operation = operationRegisterService.registerDeleteWorkspace(ownerUserId, workspaceId);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
            operationDeleteWorkspaceService.applyDeleteWorkspace(operation.getId(), false);
        }

        operationDeleteWorkspaceProcessor.processDeleteWorkspaceOperation(operation.getId());
    }
}
