package com.trello.project.membership.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Invitation;
import com.trello.project.membership.dto.request.InvitationRequest;

@Mapper(componentModel = "spring")
public interface InvitationRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "board", ignore = true),
            @Mapping(target = "senderUserId", ignore = true),
            @Mapping(target = "recipientUserId", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    Invitation invitationRequestToInvitation(InvitationRequest source);
}
