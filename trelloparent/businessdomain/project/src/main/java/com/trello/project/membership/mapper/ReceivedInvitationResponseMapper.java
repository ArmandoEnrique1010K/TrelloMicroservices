package com.trello.project.membership.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Invitation;
import com.trello.project.membership.dto.response.ReceivedInvitationResponse;

@Mapper(componentModel = "spring")
public interface ReceivedInvitationResponseMapper {
    @Mappings({
            @Mapping(target = "senderUser", ignore = true),
            @Mapping(target = "boardName", source = "board.name"),
            @Mapping(target = "workspaceName", source = "board.workspace.name")

    })
    ReceivedInvitationResponse invitationToReceivedInvitationResponse(Invitation source);

    List<ReceivedInvitationResponse> invitationListToReceivedInvitationResponseList(List<Invitation> source);
}
