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
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeleteWorkspaceService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteWorkspaceProcessorImpl
        extends AbstractOperationProcessor
        implements OperationDeleteWorkspaceProcessor {

    private final WorkflowClientService workflowClientService;
    private final OperationDeleteWorkspaceService operationDeleteWorkspaceService;
    private final WorkspaceProjectService workspaceProjectService;

    public OperationDeleteWorkspaceProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationDeleteWorkspaceService operationDeleteWorkspaceService,
            WorkspaceProjectService workspaceProjectService) {

        super(operationUtils, operationProjectService);

        this.workflowClientService = workflowClientService;
        this.operationDeleteWorkspaceService = operationDeleteWorkspaceService;
        this.workspaceProjectService = workspaceProjectService;
    }

    @Override
    public void processDeleteWorkspaceOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, WorkspaceNotFoundException, OperationNotPendingException,
            OperationManualReviewException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_WORKSPACE) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);
    }

    @Override
    protected void executeOriginalOperation(Operation operation) {
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

        workflowClientService.deleteAllBoardsByIdsAndMultipleBoardAccesses(boardsIds, operation.getId());
    }

    @Override
    protected void processLocalCompensation(Operation operation) {
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
            // } catch (OperationManualReviewException e) {
            // throw e;
        } catch (Exception exception) {
            operationUtils.markManualReview(operation, exception);

            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }

    // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME EXCEPTION
    // DESDE EL MICROSERVICIO WORKFLOW
    @Override
    protected void processRemoteCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            workflowClientService.revertDeleteAllBoardsByIdsAndMultipleBoardAccesses(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }
}
