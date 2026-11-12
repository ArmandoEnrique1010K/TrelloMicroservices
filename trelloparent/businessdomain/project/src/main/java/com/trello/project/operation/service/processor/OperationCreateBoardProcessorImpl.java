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
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationCreateBoardService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationCreateBoardProcessorImpl
        extends AbstractOperationProcessor
        implements OperationCreateBoardProcessor {
    private final WorkflowClientService workflowClientService;
    private final BoardProjectService boardProjectService;
    private final BoardResponseMapper boardResponseMapper;
    private final OperationCreateBoardService operationCreateBoardService;

    public OperationCreateBoardProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            BoardProjectService boardProjectService,
            BoardResponseMapper boardResponseMapper,
            OperationCreateBoardService operationCreateBoardService) {

        super(operationUtils, operationProjectService);

        this.workflowClientService = workflowClientService;
        this.boardProjectService = boardProjectService;
        this.boardResponseMapper = boardResponseMapper;
        this.operationCreateBoardService = operationCreateBoardService;
    }

    @Override
    public BoardResponse processCreateBoardOperation(UUID operationId)
            throws WorkspaceNotFoundException, BoardNotFoundException, OperationNotPendingException,
            OperationNotFoundException, OperationManualReviewException, OperationCompensatedException,
            OperationConflictException, BusinessRuleException {

        Operation operation = findOperation(operationId);

        if (operation.getType() != OperationType.CREATE_BOARD) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Solamente si el tablero existe, devolvera un boardResponse
        Board board = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);
        BoardResponse boardResponse = boardResponseMapper.boardToBoardResponse(board);
        return boardResponse;
    }

    @Override
    protected void executeOriginalOperation(Operation operation) {
        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Verificar que exista el tablero
        boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        // Llama al metodo para añadir tablero y permiso de acceso del usuario
        // propietario administrador
        workflowClientService.addBoardAndBoardAccessUserOwner(boardId, ownerUserId, operation.getId());
    }

    @Override
    protected void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationCompleted(operation);
            operationCreateBoardService.revertCreateBoard(operation.getId());

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
            workflowClientService.revertAddBoardAndBoardAccessUserOwner(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException exception) {
            operationUtils.markLocalCompensationCompleted(operation);
            log.info("EXCEPTION: " + exception);
            throw new OperationCompensatedException();
        }
    }
}
