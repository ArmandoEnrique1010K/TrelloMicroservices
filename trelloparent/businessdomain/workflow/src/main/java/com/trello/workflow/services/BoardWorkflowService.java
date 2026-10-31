package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.Board;
import com.trello.workflow.exception.BoardAccessNotFoundException;
import com.trello.workflow.exception.BoardNotFoundException;

public interface BoardWorkflowService {
    Board saveAndFlushBoard(Board board);

    boolean existsBoardByIdAndStatusTrue(UUID boardId);

    Board findBoardByIdAndStatusTrue(UUID boardId)
            throws BoardNotFoundException;

    long countBoardsByIds(List<UUID> boardsIds);

    void deleteBoardByIdAndStatusTrueAndDeleteBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId)
            throws BoardNotFoundException, BoardAccessNotFoundException;

    void disableBoardStatusByIdAndStatusTrueAndDisableAllBoardAccessByStatusTrue(UUID boardId)
            throws BoardNotFoundException;

    void enableBoardStatusByIdAndStatusFalseAndEnableAllBoardAccessByStatusFalse(UUID boardId)
            throws BoardNotFoundException;

    void disableAllBoardsStatusByIdAndStatusTrueAndDisableAllBoardAccessesByStatusTrue(List<UUID> boardsIds);

    void enableAllBoardsStatusByIdAndStatusFalseAndEnableAllBoardAccessesByStatusFalse(List<UUID> boardsIds);
}
