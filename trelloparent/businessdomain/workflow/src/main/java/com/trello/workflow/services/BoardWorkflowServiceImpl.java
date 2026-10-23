package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;
import com.trello.workflow.repositories.BoardRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.exception.BoardAccessNotFoundException;
import com.trello.workflow.exception.BoardNotFoundException;

@Service
public class BoardWorkflowServiceImpl implements BoardWorkflowService {

    private final BoardRepository boardRepository;
    private final BoardAccessRepository boardAccessRepository;

    public BoardWorkflowServiceImpl(BoardRepository boardRepository, BoardAccessRepository boardAccessRepository) {
        this.boardRepository = boardRepository;
        this.boardAccessRepository = boardAccessRepository;
    }

    // Guarda la entidad y fuerza el flush inmediatamente.

    // Esto permite que las restricciones de la base de datos se validen
    // en este punto de la ejecución, pudiendo detectar inmediatamente
    // una violación de la clave primaria, como intentar guardar un Board
    // con un ID que ya existe.
    @Override
    public Board saveAndFlushBoard(Board board) {
        return boardRepository.saveAndFlush(board);
    }

    @Override
    public boolean existsBoardById(UUID boardId) {
        return boardRepository.existsByIdAndStatusTrue(boardId);
    }

    @Override
    public Board findBoardById(UUID boardId) throws BoardNotFoundException {
        return boardRepository.findByIdAndStatusTrue(boardId).orElseThrow(BoardNotFoundException::new);
    }

    // @Override
    // public void deleteBoardById(UUID boardId) throws BoardNotFoundException {
    // if (!boardRepository.existsByIdAndStatusTrue(boardId)) {
    // throw new BoardNotFoundException();
    // }

    // boardRepository.deleteById(boardId);
    // }

    // @Override
    // public void deleteBoardsByIds(List<UUID> boardsIds) {
    // boardRepository.deleteAllById(boardsIds);
    // }

    @Override
    public long countBoardsByIds(List<UUID> boardsIds) {
        return boardRepository.countByIdIn(boardsIds);
    }

    @Override
    public void deleteBoardByIdAndDeleteBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId)
            throws BoardNotFoundException, BoardAccessNotFoundException {

        if (!boardRepository.existsByIdAndStatusTrue(boardId)) {
            throw new BoardNotFoundException();
        }

        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(boardId, userId)
                .orElseThrow(
                        BoardAccessNotFoundException::new);

        boardRepository.deleteById(boardId);
        boardAccessRepository.delete(boardAccess);
    }

    // @Override
    // public void disableBoardStatusById(UUID boardId) throws
    // BoardNotFoundException {
    // Board board =
    // boardRepository.findByIdAndStatusTrue(boardId).orElseThrow(BoardNotFoundException::new);

    // board.setStatus(false);
    // boardRepository.save(board);
    // }

    // @Override
    // public void disableBoardsStatusesByIds(List<UUID> boardsIds) {
    // List<Board> boards = boardRepository.findAllByIdInAndStatusTrue(boardsIds);

    // if (boards.isEmpty()) {
    // return;
    // }

    // boards.forEach(board -> board.setStatus(false));
    // boardRepository.saveAll(boards);
    // }

    @Override
    public void enableBoardStatusById(UUID boardId) throws BoardNotFoundException {
        Board board = boardRepository.findByIdAndStatusFalse(boardId).orElseThrow(BoardNotFoundException::new);

        board.setStatus(true);
        boardRepository.save(board);
    }

    // @Override
    // public void enableBoardsStatusesByIds(List<UUID> boardsIds) {
    // List<Board> boards = boardRepository.findAllByIdInAndStatusFalse(boardsIds);

    // if (boards.isEmpty()) {
    // return;
    // }

    // boards.forEach(board -> board.setStatus(true));
    // boardRepository.saveAll(boards);
    // }

    @Override
    public void disableBoardStatusByIdAndDisableAllBoardAccessStatus(UUID boardId)
            throws BoardNotFoundException, BoardAccessNotFoundException {

        // Desactivar tablero
        Board board = boardRepository.findByIdAndStatusTrue(boardId).orElseThrow(BoardNotFoundException::new);

        board.setStatus(false);
        boardRepository.save(board);

        // Desactivar todos los permisos de acceso del tablero
        List<BoardAccess> boardAcceses = boardAccessRepository.findAllByBoardIdAndStatusTrue(boardId);

        if (boardAcceses.isEmpty()) {
            return;
        }

        // Deshabilitar de todos boardAccess el estado sin excepciones
        boardAcceses.forEach(boardAccess -> boardAccess.setStatus(false));
        boardAccessRepository.saveAll(boardAcceses);
    }

    @Override
    public void disableAllBoardsStatusByIdAndDisableAllBoardAccessesStatus(List<UUID> boardsIds) {
        List<Board> boards = boardRepository.findAllByIdInAndStatusTrue(boardsIds);

        if (boards.isEmpty()) {
            return;
        }

        boards.forEach(board -> board.setStatus(false));
        boardRepository.saveAll(boards);

        // De la lista de tableros, tomar los boardsAccess de cada uno y cambiar su
        // estado a false
        // Con el ID de un tablero
        // List<BoardAccess> boardAccesses =
        // boardAccessRepository.findAllByBoardIdAndStatusTrue(board);
        boardAccessRepository.disableAllByBoardIds(boardsIds);
    }

    @Override
    public void enableAllBoardsStatusByIdAndEnableAllBoardAccessesStatus(List<UUID> boardsIds) {
        List<Board> boards = boardRepository.findAllByIdInAndStatusTrue(boardsIds);

        if (boards.isEmpty()) {
            return;
        }

        boards.forEach(board -> board.setStatus(true));
        boardRepository.saveAll(boards);

        // De la lista de tableros, tomar los boardsAccess de cada uno y cambiar su
        // estado a false
        // Con el ID de un tablero
        // List<BoardAccess> boardAccesses =
        // boardAccessRepository.findAllByBoardIdAndStatusTrue(board);
        boardAccessRepository.enableAllByBoardIds(boardsIds);
    }
}
