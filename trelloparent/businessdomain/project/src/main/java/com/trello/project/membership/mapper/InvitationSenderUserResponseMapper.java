package com.trello.project.membership.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Invitation;
import com.trello.project.membership.dto.response.InvitationSenderUserResponse;

@Mapper(componentModel = "spring")
public interface InvitationSenderUserResponseMapper {

    @Mappings({
            // Campos a ignorar
            @Mapping(target = "senderUser", ignore = true),
            @Mapping(target = "recipientUser", ignore = true)
    })
    InvitationSenderUserResponse invitationToInvitationSenderUserResponse(Invitation source);

    List<InvitationSenderUserResponse> invitationListToInvitationSenderUserResponseList(List<Invitation> source);
}
