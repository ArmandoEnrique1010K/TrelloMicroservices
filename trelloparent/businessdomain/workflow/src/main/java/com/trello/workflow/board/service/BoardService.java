package com.trello.workflow.board.service;

import java.util.UUID;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;

public interface BoardService {

    void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId) throws BoardAlreadyExistsException;

    void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId);
}
