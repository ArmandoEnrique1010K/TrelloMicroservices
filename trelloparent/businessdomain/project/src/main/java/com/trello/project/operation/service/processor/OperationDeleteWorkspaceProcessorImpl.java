package com.trello.project.operation.service.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.entities.Operation;
import com.trello.project.entities.Workspace;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeleteWorkspaceService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

@Service
public class OperationDeleteWorkspaceProcessorImpl implements OperationDeleteWorkspaceProcessor {

    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final OperationUtils operationUtils;
    private final OperationDeleteWorkspaceService operationDeleteWorkspaceService;
    private final WorkspaceProjectService workspaceProjectService;

    public OperationDeleteWorkspaceProcessorImpl(
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationUtils operationUtils,
            OperationDeleteWorkspaceService operationDeleteWorkspaceService,
            WorkspaceProjectService workspaceProjectService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.operationUtils = operationUtils;
        this.operationDeleteWorkspaceService = operationDeleteWorkspaceService;
        this.workspaceProjectService = workspaceProjectService;
    }

    @Override
    public void processDeleteWorkspaceOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_WORKSPACE) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);
    }

    private void processOriginalOperation(Operation operation) {
        operationUtils.markExecuting(operation);
        try {
            processDeleteWorkspace(operation);
            operationUtils.markCompleted(operation);

        } catch (ServiceUnavailableException e) {
            operationUtils.markCompensationPending(operation);

            try {
                processLocalCompensation(operation);
                processRemoteCompensation(operation);

            } catch (ServiceUnavailableException compensationException) {
                Operation updatedOperation = operationProjectService
                        .findOperationById(operation.getId())
                        .orElseThrow(OperationNotFoundException::new);

                if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                    throw new OperationCompensatedException();
                }

                throw compensationException;
            }

            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                throw new OperationCompensatedException();
            }

            // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME EXCEPTION
            // DESDE EL MICROSERVICIO WORKFLOW
        } catch (Exception e) {
            operationUtils.markManualReview(operation, e);
            throw new BusinessRuleException();
        }

    }

    private void processDeleteWorkspace(Operation operation) {
        UUID workspaceId = operation.getWorkspaceId();
        UUID ownerUserId = operation.getOwnerUserId();

        // El estado no importa
        Workspace workspace = workspaceProjectService.findWorkspaceByIdAndOwnerUserId(workspaceId, ownerUserId);

        List<UUID> boardsIds = new ArrayList<>();

        // Obtener la lista de boardsIds y pasarlas al cliente
        if (workspace.getBoards() != null) {
            boardsIds = workspace.getBoards().stream()
                    .filter(board -> board != null && board.getId() != null)
                    .map(board -> board.getId())
                    .collect(Collectors.toList());
        }

        workflowClientService.deleteManyBoardsByIdsAndBoardAccessAndTasks(boardsIds, operation.getId());
    }

    private void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationCompleted(operation);
            operationDeleteWorkspaceService.revertDeleteWorkspace(operation.getId());
            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.MANUAL_INTERVENTION_REQUIRED) {
                throw new OperationManualReviewException();
            }
            operationUtils.markLocalCompensationCompleted(updatedOperation);
        } catch (OperationManualReviewException e) {
            throw e;
        } catch (Exception e) {
            operationUtils.markManualReview(operation, e);
            throw new OperationManualReviewException();
        }

    }

    private void processRemoteCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            workflowClientService.revertDeleteManyBoardsByIdsAndBoardAccessAndTasks(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }

}
