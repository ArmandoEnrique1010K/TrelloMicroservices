package com.trello.identity.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.trello.identity.dtos.AccountRequest;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountRequestMapper {

    User accountRequestToUser(AccountRequest source);

    @InheritInverseConfiguration
    AccountRequest userToAccountRequest(User source);
}
