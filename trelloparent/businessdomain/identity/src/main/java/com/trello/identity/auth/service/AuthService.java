package com.trello.identity.auth.service;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.auth.dto.AuthenticationRequest;
import com.trello.identity.auth.dto.AuthenticationResponse;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;

public interface AuthService {
    AccountResponse createAccount(AccountRequest accountRequest)
            throws MismatchPasswordException, UserAlreadyExistsException;

    AuthenticationResponse login(AuthenticationRequest authenticationRequest);

    void logout();
}
