package com.trello.project.membership.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.project.entities.Invitation;
import com.trello.project.membership.dto.response.InvitationResponse;

@Mapper(componentModel = "spring")
public interface InvitationResponseMapper {

    InvitationResponse invitationToInvitationResponse(Invitation source);

    List<InvitationResponse> invitationListToInvitationResponseList(List<Invitation> source);
}
