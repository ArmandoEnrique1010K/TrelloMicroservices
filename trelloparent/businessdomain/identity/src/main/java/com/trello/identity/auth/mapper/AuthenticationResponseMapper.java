package com.trello.identity.auth.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.auth.dto.AuthenticationResponse;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AuthenticationResponseMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "firstName", ignore = true),
            @Mapping(target = "lastName", ignore = true),
            @Mapping(target = "email", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "otpToken", ignore = true)
    })
    User authenticationResponseToUser(AuthenticationResponse source);

    @Mappings({
            @Mapping(target = "accessToken", ignore = true),
            @Mapping(target = "refreshToken", ignore = true),
            @Mapping(target = "expiresIn", ignore = true),
    })
    @InheritInverseConfiguration
    AuthenticationResponse userToAuthenticationResponse(User source);
}
