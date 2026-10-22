package com.trello.workflow.board.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.operation.service.processor.OperationProcessor;
import com.trello.workflow.operation.service.register.OperationRegisterService;

@Service
public class BoardServiceImpl implements BoardService {

    private final OperationProcessor operationProcessor;
    private final OperationRegisterService operationRegisterService;

    public BoardServiceImpl(
            OperationProcessor operationProcessor,
            OperationRegisterService operationRegisterService) {
        this.operationProcessor = operationProcessor;
        this.operationRegisterService = operationRegisterService;
    }

    @Override
    public void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId,
            UUID operationId) throws BoardAlreadyExistsException {
        Operation operation = operationRegisterService.registerAddBoardAndBoardAccessUserOwner(boardId, userId,
                operationId);
        operationProcessor.processOperation(operation.getId());
    }

    @Override
    public void deleteBoardAndBoardAccessAndTasks(UUID boardId, UUID operationId) {
        Operation operation = operationRegisterService.registerDeleteBoardAndBoardAccessAndTasks(boardId, operationId);
        operationProcessor.processOperation(operation.getId());
    }

    @Override
    public void deleteAllBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardsIds, UUID operationId) {
        Operation operation = operationRegisterService.registerDeleteAllBoardsAndBoardAccessAndTasks(boardsIds,
                operationId);
        operationProcessor.processOperation(operation.getId());
    }
}
