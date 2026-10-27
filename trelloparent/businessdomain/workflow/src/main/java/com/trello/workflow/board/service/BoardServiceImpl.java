package com.trello.workflow.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.operation.service.processor.OperationProcessor;
import com.trello.workflow.operation.service.register.OperationRegisterService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class BoardServiceImpl implements BoardService {

    private final OperationProcessor operationProcessor;
    private final OperationRegisterService operationRegisterService;
    private final OperationWorkflowService operationWorkflowService;

    public BoardServiceImpl(
            OperationProcessor operationProcessor,
            OperationRegisterService operationRegisterService,
            OperationWorkflowService operationWorkflowService) {
        this.operationProcessor = operationProcessor;
        this.operationRegisterService = operationRegisterService;
        this.operationWorkflowService = operationWorkflowService;
    }

    @Override
    public void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId,
            UUID operationId) throws BoardAlreadyExistsException {
        Operation operation = operationRegisterService.registerAddBoardAndBoardAccessUserOwner(boardId, userId,
                operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

    @Override
    public void deleteBoardAndBoardAccessAndTasks(UUID boardId, UUID operationId) {
        Operation operation = operationRegisterService.registerDeleteBoardAndBoardAccessAndTasks(boardId, operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

    @Override
    public void deleteAllBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardsIds, UUID operationId) {
        Operation operation = operationRegisterService.registerDeleteAllBoardsAndBoardAccessAndTasks(boardsIds,
                operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
