package com.trello.project.board.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.dto.response.BoardMembershipResponse;
import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.exception.BoardAlreadyExistsException;

public interface BoardService {

    // idempotencyKey es una clave aleatoria que se puede pasar desde el frontend
    // como por ejemplo desde un formulario en una ventana modal
    BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest,
            UUID idempotencyKey)
            throws BoardAlreadyExistsException;

    List<BoardResponse> listAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId);

    BoardResponse editBoard(UUID ownerUserId, UUID boardId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException;

    void deleteBoard(UUID ownerUserId, UUID boardId);

    // Listar los UUIDs de miembros e invitados por id de board
    List<UUID> listAllMembersIdsAndInvitationsIdsByBoardId(UUID ownerUserId, UUID boardId);

    // Listar los tableros en el cual el usuario autenticado sea miembro sin
    // importar el rol
    List<BoardMembershipResponse> listAllBoardsByMemberUserId(UUID memberUserId);
}
