package com.trello.project.invitation.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Invitation;
import com.trello.project.invitation.dto.response.BoardInvitationResponse;

@Mapper(componentModel = "spring")
public interface BoardInvitationResponseMapper {

    @Mappings({
            @Mapping(target = "recipientUser", ignore = true)
    })
    BoardInvitationResponse invitationToBoardInvitationResponse(Invitation source);

    List<BoardInvitationResponse> invitationListToBoardInvitationResponseList(List<Invitation> source);
}
