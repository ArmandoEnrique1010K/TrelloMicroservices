package com.trello.identity.mapper.auth;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.trello.identity.dtos.auth.AccountResponse;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountResponseMapper {

    User accountResponseToUser(AccountResponse source);

    @InheritInverseConfiguration
    AccountResponse userToAccountResponse(User source);
}
