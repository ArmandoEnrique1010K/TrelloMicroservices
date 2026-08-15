package com.trello.identity.auth.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountResponseMapper {

    @Mappings({
            @Mapping(target = "firstName", ignore = true),
            @Mapping(target = "lastName", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "otpToken", ignore = true)
    })
    User accountResponseToUser(AccountResponse source);

    @InheritInverseConfiguration
    AccountResponse userToAccountResponse(User source);
}
