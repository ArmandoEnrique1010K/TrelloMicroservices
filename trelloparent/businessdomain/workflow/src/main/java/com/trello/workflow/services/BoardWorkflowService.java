package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.Board;
import com.trello.workflow.exception.BoardNotFoundException;

public interface BoardWorkflowService {
    Board saveAndFlushBoard(Board board);

    boolean existsBoardById(UUID boardId);

    Board findBoardById(UUID boardId) throws BoardNotFoundException;

    void deleteBoardById(UUID boardId) throws BoardNotFoundException;

    void deleteBoardsByIds(List<UUID> boardsIds);
}
