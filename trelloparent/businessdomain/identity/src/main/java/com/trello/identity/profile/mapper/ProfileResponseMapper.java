package com.trello.identity.profile.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.entities.User;
import com.trello.identity.profile.dto.ProfileResponse;

@Mapper(componentModel = "spring")
public interface ProfileResponseMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "confirmed", ignore = true),
            @Mapping(target = "otpToken", ignore = true),
    })
    User profileResponseToUser(ProfileResponse source);

    @InheritInverseConfiguration
    ProfileResponse userToProfileResponse(User source);
}
