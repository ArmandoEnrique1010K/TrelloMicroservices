package com.trello.identity.auth.mapper;

import org.mapstruct.Mapper;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountResponseMapper {

    AccountResponse userToAccountResponse(User source);
}
