package com.trello.project.workspace.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Board;
import com.trello.project.workspace.dto.request.BoardRequest;

@Mapper(componentModel = "spring")
public interface BoardRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "workspace", ignore = true),
            @Mapping(target = "members", ignore = true),
            @Mapping(target = "invitations", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    Board boardRequestToBoard(BoardRequest source);
}
