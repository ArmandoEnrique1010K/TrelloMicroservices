package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteBoardServiceImpl implements OperationDeleteBoardService {
    private final OperationProjectService operationProjectService;
    private final BoardProjectService boardProjectService;

    public OperationDeleteBoardServiceImpl(
            OperationProjectService operationProjectService,
            BoardProjectService boardProjectService) {
        this.operationProjectService = operationProjectService;
        this.boardProjectService = boardProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteBoard(UUID operationId, boolean automatic)
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

        UUID boardId = persistedOperation.getBoardId();
        UUID ownerUserId = persistedOperation.getOwnerUserId();

        log.info("BUSCANDO");

        // Verificar si existe tablero
        boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        log.info("CAMBIANDO ESTADO");

        // En lugar de borrar el tablero se va a cambiar su estado
        boardProjectService.changeStatusBoardById(boardId, ownerUserId, false);
        log.info("GUARDANDO");

        operationProjectService.saveOperation(persistedOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteBoard(UUID operationId) throws OperationNotFoundException {
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
