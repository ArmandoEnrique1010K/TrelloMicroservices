package com.trello.project.operation.service.handler;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.utils.OperationHandlerUtils;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

@Service
public class OperationDeleteWorkspaceServiceImpl implements OperationDeleteWorkspaceService {
    private final OperationProjectService operationProjectService;
    private final WorkspaceProjectService workspaceProjectService;
    private final OperationHandlerUtils operationHandlerUtils;

    public OperationDeleteWorkspaceServiceImpl(
            OperationProjectService operationProjectService,
            WorkspaceProjectService workspaceProjectService,
            OperationHandlerUtils operationHandlerUtils) {
        this.operationProjectService = operationProjectService;
        this.workspaceProjectService = workspaceProjectService;
        this.operationHandlerUtils = operationHandlerUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteWorkspace(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, WorkspaceNotFoundException {

        Operation operation = operationHandlerUtils
                .prepareExecution(operationId, automatic);

        UUID workspaceId = operation.getWorkspaceId();
        UUID ownerUserId = operation.getOwnerUserId();

        workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(workspaceId, ownerUserId);

        // En lugar de eliminar el espacio de trabajo se va a cambiar su estado
        // Además del estado de todos los tableros incluidos
        workspaceProjectService.changeStatusWorkspaceById(workspaceId, ownerUserId, false);

        operationProjectService.saveOperation(operation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteWorkspace(UUID operationId)
            throws OperationNotFoundException, WorkspaceNotFoundException {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        UUID workspaceId = operation.getWorkspaceId();
        UUID ownerUserId = operation.getOwnerUserId();

        workspaceProjectService.changeStatusWorkspaceById(workspaceId, ownerUserId,
                true);
    }
}
