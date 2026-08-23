package com.trello.identity.profile.mapper;

import org.mapstruct.Mapper;

import com.trello.identity.entities.User;
import com.trello.identity.profile.dto.response.ProfileResponse;

@Mapper(componentModel = "spring")
public interface ProfileResponseMapper {
    ProfileResponse userToProfileResponse(User source);
}
