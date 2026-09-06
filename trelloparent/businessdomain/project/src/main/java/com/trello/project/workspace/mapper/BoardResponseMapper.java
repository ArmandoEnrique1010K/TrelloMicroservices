package com.trello.project.workspace.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.project.entities.Board;
import com.trello.project.workspace.dto.response.BoardResponse;

@Mapper(componentModel = "spring")
public interface BoardResponseMapper {
    BoardResponse boardToBoardResponse(Board source);

    List<BoardResponse> boardListToBoardResponseList(List<Board> source);
}
