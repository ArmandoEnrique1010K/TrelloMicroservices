package com.trello.project.board.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.entities.Board;

@Mapper(componentModel = "spring")
public interface BoardResponseMapper {
    BoardResponse boardToBoardResponse(Board source);

    List<BoardResponse> boardListToBoardResponseList(List<Board> source);
}
