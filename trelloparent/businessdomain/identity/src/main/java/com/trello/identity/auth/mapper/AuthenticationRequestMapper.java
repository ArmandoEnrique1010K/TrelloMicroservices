package com.trello.identity.auth.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.auth.dto.AuthenticationRequest;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AuthenticationRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "firstName", ignore = true),
            @Mapping(target = "lastName", ignore = true),
            @Mapping(target = "confirmed", ignore = true),
            @Mapping(target = "otpToken", ignore = true),
    })
    User authenticationRequestToUser(AuthenticationRequest source);

    @InheritInverseConfiguration
    AuthenticationRequest userToAuthenticationRequest(User source);
}
