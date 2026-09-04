package com.trello.project.workspace.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Workspace;
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

    public WorkspaceServiceImpl(WorkspaceProjectService workspaceProjectService,
            WorkspaceRequestMapper workspaceRequestMapper, WorkspaceResponseMapper workspaceResponseMapper) {
        this.workspaceProjectService = workspaceProjectService;
        this.workspaceRequestMapper = workspaceRequestMapper;
        this.workspaceResponseMapper = workspaceResponseMapper;
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

        Workspace findedWorkspace = workspaceProjectService.findWorkspaceByIdAndOwnerUserId(workspaceId, ownerUserId);

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
        workspaceProjectService.deleteWorkspaceByIdAndOwnerUserId(workspaceId, ownerUserId);
    }

}
