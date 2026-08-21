package com.trello.identity.auth.service;

import org.springframework.security.authentication.BadCredentialsException;

import com.trello.identity.auth.dto.AccountRequest;
import com.trello.identity.auth.dto.AccountResponse;
import com.trello.identity.auth.dto.AuthenticationRequest;
import com.trello.identity.auth.dto.AuthenticationResponse;
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
