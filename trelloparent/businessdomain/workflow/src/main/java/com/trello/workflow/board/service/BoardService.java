package com.trello.workflow.board.service;

import java.util.List;
import java.util.UUID;

public interface BoardService {
    void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId,
            UUID operationId);

    void deleteBoardAndBoardAccessAndTasks(UUID boardId, UUID operationId);

    void deleteAllBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardsIds);
}
