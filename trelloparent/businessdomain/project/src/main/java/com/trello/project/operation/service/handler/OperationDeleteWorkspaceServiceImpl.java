package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

@Service
public class OperationDeleteWorkspaceServiceImpl implements OperationDeleteWorkspaceService {
    private final OperationProjectService operationProjectService;
    private final WorkspaceProjectService workspaceProjectService;

    public OperationDeleteWorkspaceServiceImpl(
            OperationProjectService operationProjectService,
            WorkspaceProjectService workspaceProjectService) {
        this.operationProjectService = operationProjectService;
        this.workspaceProjectService = workspaceProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteWorkspace(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException {
        Operation persistedOperation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        OperationStatus currentStatus = persistedOperation.getStatus();

        if (currentStatus != OperationStatus.REGISTERED
                && currentStatus != OperationStatus.LOCAL_COMPENSATION_COMPLETED
                && currentStatus != OperationStatus.COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        if (automatic) {
            persistedOperation.setAutomaticAttempts(
                    persistedOperation.getAutomaticAttempts() + 1);
        } else {
            persistedOperation.setManualAttempts(
                    persistedOperation.getManualAttempts() + 1);
        }

        persistedOperation.setStatus(OperationStatus.EXECUTION_PENDING);
        persistedOperation.setCompletedAt(null);
        persistedOperation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        UUID workspaceId = persistedOperation.getWorkspaceId();
        UUID ownerUserId = persistedOperation.getOwnerUserId();

        workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(workspaceId, ownerUserId);

        // En lugar de eliminar el espacio de trabajo se va a cambiar su estado
        // Además del estado de todos los tableros incluidos
        workspaceProjectService.changeStatusWorkspaceById(workspaceId, ownerUserId, false);

        operationProjectService.saveOperation(persistedOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteWorkspace(UUID operationId) throws OperationNotFoundException {
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
