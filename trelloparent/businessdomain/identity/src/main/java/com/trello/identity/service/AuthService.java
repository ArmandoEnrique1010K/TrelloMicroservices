package com.trello.identity.service;

import com.trello.identity.dtos.auth.AccountRequest;
import com.trello.identity.dtos.auth.AccountResponse;
import com.trello.identity.exception.BusinessRuleException;

public interface AuthService {
    AccountResponse createAccount(AccountRequest accountRequest) throws BusinessRuleException;

    String login();

    void logout();
}
