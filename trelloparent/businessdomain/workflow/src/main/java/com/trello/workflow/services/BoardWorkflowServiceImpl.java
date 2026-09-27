package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardRepository;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Board;
import com.trello.workflow.exception.BoardNotFoundException;

@Service
public class BoardWorkflowServiceImpl implements BoardWorkflowService {

    private final BoardRepository boardRepository;

    BoardWorkflowServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
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
        return boardRepository.existsById(boardId);
    }

    @Override
    public Board findBoardById(UUID boardId) throws BoardNotFoundException {
        return boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
    }

    @Override
    public void deleteBoardById(UUID boardId) throws BoardNotFoundException {
        if (!boardRepository.existsById(boardId)) {
            throw new BoardNotFoundException();
        }

        boardRepository.deleteById(boardId);
    }
}
