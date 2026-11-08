package com.trello.project.operation.service.handler;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.utils.OperationHandlerUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteBoardServiceImpl implements OperationDeleteBoardService {
    private final OperationProjectService operationProjectService;
    private final BoardProjectService boardProjectService;
    private final OperationHandlerUtils operationHandlerUtils;

    public OperationDeleteBoardServiceImpl(
            OperationProjectService operationProjectService,
            BoardProjectService boardProjectService,
            OperationHandlerUtils operationHandlerUtils) {
        this.operationProjectService = operationProjectService;
        this.boardProjectService = boardProjectService;
        this.operationHandlerUtils = operationHandlerUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteBoard(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, WorkspaceNotFoundException,
            BoardNotFoundException {

        Operation operation = operationHandlerUtils
                .prepareExecution(operationId, automatic);

        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // Verificar si existe tablero
        boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        // En lugar de borrar el tablero se va a cambiar su estado
        boardProjectService.changeStatusBoardById(boardId, ownerUserId, false);

        operationProjectService.saveOperation(operation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteBoard(UUID operationId)
            throws OperationNotFoundException, BoardNotFoundException {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID ownerUserId = operation.getOwnerUserId();

        // A su vez este metodo busca si existe el tablero por su ID y lo guarda
        boardProjectService.changeStatusBoardById(boardId, ownerUserId, true);
    }
}
