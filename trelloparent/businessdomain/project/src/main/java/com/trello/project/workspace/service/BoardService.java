package com.trello.project.workspace.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.workspace.dto.request.BoardRequest;
import com.trello.project.workspace.dto.response.BoardResponse;
import com.trello.project.workspace.exception.BoardAlreadyExistsException;

public interface BoardService {
    BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException;

    List<BoardResponse> listAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId);

    BoardResponse editBoard(UUID ownerUserId, UUID boardId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException;

    void deleteBoard(UUID ownerUserId, UUID boardId);
}
