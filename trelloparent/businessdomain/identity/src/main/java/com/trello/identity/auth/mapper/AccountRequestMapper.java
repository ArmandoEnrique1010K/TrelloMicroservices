package com.trello.identity.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.identity.auth.dto.request.AccountRequest;
import com.trello.identity.entities.User;

@Mapper(componentModel = "spring")
public interface AccountRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "confirmed", ignore = true),
            @Mapping(target = "otpToken", ignore = true),
            @Mapping(target = "refreshTokens", ignore = true)
    })
    User accountRequestToUser(AccountRequest source);
}
