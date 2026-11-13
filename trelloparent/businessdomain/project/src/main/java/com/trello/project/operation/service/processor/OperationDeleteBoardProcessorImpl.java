package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeleteBoardService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteBoardProcessorImpl
        extends AbstractOperationProcessor
        implements OperationDeleteBoardProcessor {

    private final WorkflowClientService workflowClientService;
    private final OperationDeleteBoardService operationDeleteBoardService;
    private final BoardProjectService boardProjectService;

    public OperationDeleteBoardProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationDeleteBoardService operationDeleteBoardService,
            BoardProjectService boardProjectService) {

        super(operationUtils, operationProjectService);

        this.workflowClientService = workflowClientService;
        this.operationDeleteBoardService = operationDeleteBoardService;
        this.boardProjectService = boardProjectService;
    }

    @Override
    public void processDeleteBoardOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, WorkspaceNotFoundException, BoardNotFoundException, OperationNotPendingException,
            OperationManualReviewException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_BOARD) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);

    }

    @Override
    protected void executeOriginalOperation(Operation operation) {
        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // El estado del tablero no importa
        boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        workflowClientService.deleteBoardAndBoardAccesses(boardId, operation.getId());
    }

    @Override
    protected void processLocalCompensation(Operation operation) {
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
            workflowClientService.revertDeleteBoardAndBoardAccesses(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }

    @Override
    public void processCancelOperationsByBoardId(UUID boardId, UUID operationId) {

        try {

            // Indicar que se está ejecutando la limpieza remota.
            operationProjectService.updateOperationCleanupStatusByBoardIdAndStatus(
                    CleanupStatus.EXECUTING,
                    boardId,
                    OperationStatus.CANCELED);

            workflowClientService.cancelOperationsByBoardId(boardId);

            // Confirmar que la limpieza remota terminó correctamente.
            operationProjectService.updateOperationCleanupStatusByBoardIdAndStatus(
                    CleanupStatus.COMPLETED,
                    boardId,
                    OperationStatus.CANCELED);

        } catch (ServiceUnavailableException e) {

            log.warn(
                    "Unable to cancel operations associated with board {} in Workflow. Operation {} will require a retry.",
                    boardId,
                    operationId);

            // REVERTIR ESTADO DE OPERACIONES EN PROJECT CAMPO CLEANUP A PENDING

            // Vuelve al estado original, implica que no se pudo realizar el llamado en el
            // microservicio Workflow para cancelar la operacioin
            // Restaurar el estado de limpieza para permitir un nuevo intento.
            operationProjectService.updateOperationCleanupStatusByBoardIdAndStatus(
                    CleanupStatus.PENDING,
                    boardId,
                    OperationStatus.CANCELED);

            throw e;
        }
    }
}
