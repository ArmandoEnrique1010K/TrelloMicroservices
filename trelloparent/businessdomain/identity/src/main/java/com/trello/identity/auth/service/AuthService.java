package com.trello.identity.auth.service;

import org.springframework.security.authentication.BadCredentialsException;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.auth.dto.request.AccountRequest;
import com.trello.identity.auth.dto.request.AuthenticationRequest;
import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.exception.UserNotFoundException;

public interface AuthService {
    AccountResponse createAccount(AccountRequest accountRequest)
            throws UserNotFoundException, MismatchPasswordException, UserAlreadyExistsException;

    AuthenticationResponse login(AuthenticationRequest authenticationRequest)
            throws UserNotFoundException, BadCredentialsException, CustomBadCredentialsException;

    void logout();
}
