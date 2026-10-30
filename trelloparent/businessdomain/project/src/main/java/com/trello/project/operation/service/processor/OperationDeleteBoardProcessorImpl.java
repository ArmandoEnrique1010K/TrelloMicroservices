package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeleteBoardService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteBoardProcessorImpl implements OperationDeleteBoardProcessor {

    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final OperationUtils operationUtils;
    private final OperationDeleteBoardService operationDeleteBoardService;
    private final BoardProjectService boardProjectService;

    public OperationDeleteBoardProcessorImpl(
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationUtils operationUtils,
            OperationDeleteBoardService operationDeleteBoardService,
            BoardProjectService boardProjectService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.operationUtils = operationUtils;
        this.operationDeleteBoardService = operationDeleteBoardService;
        this.boardProjectService = boardProjectService;
    }

    @Override
    public void processDeleteBoardOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_BOARD) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);

    }

    private void processOriginalOperation(Operation operation) {
        operationUtils.markExecuting(operation);
        try {
            processDeleteBoard(operation);
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

    private void processDeleteBoard(Operation operation) {
        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        log.info("TABLERO ENCONTRADO");
        // El estado del tablero no importa
        Board board = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        log.info("TABLERO ID: " + board.getId());
        workflowClientService.deleteBoardByIdAndBoardAccessAndTasks(boardId, operation.getId());
    }

    private void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationCompleted(operation);
            operationDeleteBoardService.revertDeleteBoard(operation.getId());
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
            workflowClientService.revertDeleteBoardByIdAndBoardAccessAndTasks(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }

}
