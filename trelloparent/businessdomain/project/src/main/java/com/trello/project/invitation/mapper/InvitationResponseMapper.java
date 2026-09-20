package com.trello.project.invitation.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.project.entities.Invitation;
import com.trello.project.invitation.dto.response.InvitationResponse;

@Mapper(componentModel = "spring")
public interface InvitationResponseMapper {

    InvitationResponse invitationToInvitationResponse(Invitation source);

    List<InvitationResponse> invitationListToInvitationResponseList(List<Invitation> source);
}
