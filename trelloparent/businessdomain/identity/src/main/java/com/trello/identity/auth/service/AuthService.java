package com.trello.identity.auth.service;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.exception.BusinessRuleException;

public interface AuthService {
    AccountResponse createAccount(AccountRequest accountRequest) throws BusinessRuleException;

    String login();

    void logout();
}
