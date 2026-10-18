package com.trello.project.board.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.board.dto.response.BoardMembershipResponse;
import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.enums.Role;

@Mapper(componentModel = "spring")
public interface BoardMembershipResponseMapper {
    @Mappings({
            @Mapping(target = "userDataAvailable", ignore = true),
            @Mapping(target = "ownerUser", ignore = true),
            @Mapping(target = "role", ignore = true)
    })
    BoardMembershipResponse boardToBoardMembershipResponse(Board source);

    // Obtiene el rol del usuario dentro de la colección de miembros
    default Role mapRole(List<Member> members, UUID memberUserId) {
        if (members == null || memberUserId == null) {
            return null;
        }

        return members.stream()
                .filter(member -> member.getUserId().equals(memberUserId))
                .map(member -> member.getRole())
                .findFirst()
                .orElse(null);
    }

    // Mapea un tablero y asigna el rol del usuario consultado.
    default BoardMembershipResponse mapBoard(
            Board board,
            UUID memberUserId) {

        BoardMembershipResponse response = boardToBoardMembershipResponse(board);

        response.setRole(mapRole(board.getMembers(), memberUserId));

        return response;
    }

    // Este método se utiliza en el servicio...
    // Convierte la lista de tableros y asigna el rol correspondiente
    // a cada respuesta.
    default List<BoardMembershipResponse> boardListToBoardMembershipResponseList(
            List<Board> boards,
            UUID memberUserId) {

        if (boards == null) {
            return List.of();
        }

        return boards.stream()
                .map(board -> mapBoard(board, memberUserId))
                .toList();
    }
}
