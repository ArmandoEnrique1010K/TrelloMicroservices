package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.mapper.BoardResponseMapper;
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
import com.trello.project.operation.service.handler.OperationAddBoardAndBoardAccessUserOwnerService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationAddBoardAndBoardAccessUserOwnerProcessorImpl
        implements OperationAddBoardAndBoardAccessUserOwnerProcessor {
    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final OperationUtils operationUtils;
    private final BoardProjectService boardProjectService;
    private final BoardResponseMapper boardResponseMapper;
    private final OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService;

    public OperationAddBoardAndBoardAccessUserOwnerProcessorImpl(
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationUtils operationUtils,
            BoardProjectService boardProjectService,
            BoardResponseMapper boardResponseMapper,
            OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.operationUtils = operationUtils;
        this.boardProjectService = boardProjectService;
        this.boardResponseMapper = boardResponseMapper;
        this.operationAddBoardAndBoardAccessUserOwnerService = operationAddBoardAndBoardAccessUserOwnerService;
    }

    @Override
    public BoardResponse processAddBoardAndBoardAccessUserOwnerOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException {

        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.CREATE_BOARD) {
            throw new OperationConflictException();
        }

        return processOriginalOperation(operation);
    }

    private BoardResponse processOriginalOperation(Operation operation)
            throws BusinessRuleException, OperationNotFoundException,
            OperationCompensatedException, OperationManualReviewException {

        operationUtils.markExecuting(operation);

        try {
            processAddBoardAndBoardAccessUserOwner(operation);
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

            // En lugar de devolver un null, se devuelve la misma excepción porque el
            // tablero ya fue eliminado
            // return null;
            throw new OperationCompensatedException();
            // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME EXCEPTION
            // DESDE EL MICROSERVICIO WORKFLOW

        } catch (Exception e) {
            operationUtils.markManualReview(operation, e);
            throw new BusinessRuleException();
        }

        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Solamente si el tablero existe, devolvera un boardResponse
        Board board = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);
        BoardResponse boardResponse = boardResponseMapper.boardToBoardResponse(board);
        return boardResponse;

    }

    private void processAddBoardAndBoardAccessUserOwner(Operation operation) {
        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Verificar que exista el tablero
        boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        workflowClientService.addBoardAndBoardAccessUserOwner(boardId, operation.getId());
    }

    private void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationCompleted(operation);
            operationAddBoardAndBoardAccessUserOwnerService.revertAddBoardAndBoardAccessUserOwner(operation.getId());

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
            workflowClientService.revertAddBoardAndBoardAccessUserOwner(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }
}
