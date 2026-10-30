package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.entities.Board;
import com.trello.project.entities.Operation;
import com.trello.project.entities.Workspace;
import com.trello.project.enums.OperationStatus;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

@Service
public class OperationAddBoardAndBoardAccessUserOwnerServiceImpl
        implements OperationAddBoardAndBoardAccessUserOwnerService {

    private final OperationProjectService operationProjectService;
    private final BoardProjectService boardProjectService;
    private final WorkspaceProjectService workspaceProjectService;

    public OperationAddBoardAndBoardAccessUserOwnerServiceImpl(
            OperationProjectService operationProjectService,
            BoardProjectService boardProjectService,
            WorkspaceProjectService workspaceProjectService) {
        this.operationProjectService = operationProjectService;
        this.boardProjectService = boardProjectService;
        this.workspaceProjectService = workspaceProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAddBoardAndBoardAccessUserOwner(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, InvitationConfirmedException {
        Operation persistedOperation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        OperationStatus currentStatus = persistedOperation.getStatus();

        if (currentStatus != OperationStatus.REGISTERED
                && currentStatus != OperationStatus.LOCAL_COMPENSATION_COMPLETED
                && currentStatus != OperationStatus.COMPENSATION_COMPLETED
        // && currentStatus != OperationStatus.MANUAL_INTERVENTION_REQUIRED
        ) {
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

        UUID ownerUserId = persistedOperation.getOwnerUserId();
        String boardName = persistedOperation.getBoardName();
        String boardDescription = persistedOperation.getBoardDescription();
        UUID workspaceId = persistedOperation.getWorkspaceId();
        UUID boardId = persistedOperation.getBoardId();
        // UUID idempotencyKey = persistedOperation.getIdempotencyKey();

        Workspace workspace = workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(
                workspaceId,
                ownerUserId);

        if (boardProjectService.existsBoardByWorkspaceIdAndName(workspaceId, boardName)) {
            throw new BoardAlreadyExistsException();
        }

        // Crear el nuevo tablero
        Board board = new Board();
        board.setId(boardId);
        board.setName(boardName);
        board.setDescription(boardDescription);
        board.setWorkspace(workspace);
        board.setStatus(true);
        boardProjectService.saveBoard(board);

        operationProjectService.saveOperation(persistedOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAddBoardAndBoardAccessUserOwner(UUID operationId) throws OperationNotFoundException {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        UUID ownerUserId = operation.getOwnerUserId();
        // String boardName = operation.getBoardName();
        // String boardDescription = operation.getBoardDescription();
        // UUID workspaceId = operation.getWorkspaceId();
        UUID boardId = operation.getBoardId();

        // Debe borrar inmediatamente el tablero
        boardProjectService.deleteBoardByIdAndOwnerUserId(boardId, ownerUserId);
    }
}
