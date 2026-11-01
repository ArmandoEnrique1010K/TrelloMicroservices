package com.trello.project.board.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.dto.response.BoardMembershipResponse;
import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface BoardService {

    // SAGA PATTERN
    // idempotencyKey es una clave aleatoria que se puede pasar desde el frontend
    // como por ejemplo desde un formulario en una ventana modal
    BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest,
            UUID idempotencyKey)
            throws WorkspaceNotFoundException, OperationNotPendingException, OperationNotFoundException,
            BoardAlreadyExistsException, BoardNotFoundException, OperationManualReviewException,
            OperationCompensatedException, OperationConflictException, BusinessRuleException;

    List<BoardResponse> listAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException;

    BoardResponse editBoard(UUID ownerUserId, UUID boardId, BoardRequest boardRequest)
            throws WorkspaceNotFoundException, BoardNotFoundException, BoardAlreadyExistsException;

    // SAGA PATTERN
    void deleteBoard(UUID ownerUserId, UUID boardId)
            throws WorkspaceNotFoundException, BoardNotFoundException, OperationNotPendingException,
            OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, OperationManualReviewException;

    // Listar los tableros en el cual el usuario autenticado sea miembro sin
    // importar el rol
    List<BoardMembershipResponse> listAllBoardsByMemberUserId(UUID memberUserId);
}
