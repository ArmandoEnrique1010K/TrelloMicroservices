package com.trello.identity.auth.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "confirmed", ignore = true),
            @Mapping(target = "otpToken", ignore = true),
    })
    User accountRequestToUser(AccountRequest source);

    @Mappings({
            @Mapping(target = "passwordConfirmation", ignore = true),
    })
    @InheritInverseConfiguration
    AccountRequest userToAccountRequest(User source);
}
