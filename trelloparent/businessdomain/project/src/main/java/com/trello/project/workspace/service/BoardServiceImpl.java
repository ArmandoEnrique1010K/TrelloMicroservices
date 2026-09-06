package com.trello.project.workspace.service;

import com.trello.project.entities.Board;
import com.trello.project.service.BoardProjectService;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.workspace.dto.request.BoardRequest;
import com.trello.project.workspace.dto.response.BoardResponse;
import com.trello.project.workspace.exception.BoardAlreadyExistsException;
import com.trello.project.workspace.mapper.BoardRequestMapper;
import com.trello.project.workspace.mapper.BoardResponseMapper;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardProjectService boardProjectService;
    private final BoardRequestMapper boardRequestMapper;
    private final BoardResponseMapper boardResponseMapper;

    BoardServiceImpl(BoardProjectService boardProjectService, BoardRequestMapper boardRequestMapper,
            BoardResponseMapper boardResponseMapper) {
        this.boardProjectService = boardProjectService;
        this.boardRequestMapper = boardRequestMapper;
        this.boardResponseMapper = boardResponseMapper;
    }

    @Override
    public BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException {

        String name = boardRequest.getName();

        if (boardProjectService.existsBoardByWorkspaceIdAndName(workspaceId, ownerUserId, name)) {
            throw new BoardAlreadyExistsException();
        }

        Board boardToBoardRequest = boardRequestMapper.boardRequestToBoard(boardRequest);

        Board savedBoard = boardProjectService.saveBoard(boardToBoardRequest, ownerUserId);
        BoardResponse boardResponse = boardResponseMapper.boardToBoardResponse(savedBoard);

        return boardResponse;
    }

    @Override
    public List<BoardResponse> listAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId) {
        List<Board> listBoardsByWorkspaceId = boardProjectService.findAllBoardsByWorkspaceId(workspaceId, ownerUserId);
        return boardResponseMapper.boardListToBoardResponseList(listBoardsByWorkspaceId);
    }

    @Override
    public BoardResponse editBoard(UUID ownerUserId, UUID boardId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException {

        // String name = boardRequest.getName();
        // String description = boardRequest.getDescription();

        // Board findedBoard = boardProjectService.findBoardByIdAndWorkspaceId(boardId,
        // boardId, ownerUserId)

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'editBoard'");
    }

    @Override
    public void deleteBoard(UUID ownerUserId, UUID boardId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteBoard'");
    }

}
