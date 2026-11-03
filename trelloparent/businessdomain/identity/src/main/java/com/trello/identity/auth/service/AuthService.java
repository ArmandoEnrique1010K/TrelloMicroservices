package com.trello.identity.auth.service;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.auth.dto.request.AccountRequest;
import com.trello.identity.auth.dto.request.AuthenticationRequest;
import com.trello.identity.auth.dto.request.RefreshTokenRequest;
import com.trello.identity.auth.exception.CustomBadCredentialsException;
import com.trello.identity.auth.exception.InvalidRefreshTokenException;
import com.trello.identity.auth.exception.MismatchPasswordException;
import com.trello.identity.auth.exception.UserAlreadyExistsException;
import com.trello.identity.exception.UserNotFoundException;

public interface AuthService {
    AccountResponse createAccount(AccountRequest accountRequest)
            throws MismatchPasswordException, UserAlreadyExistsException;

    AuthenticationResponse login(AuthenticationRequest authenticationRequest)
            throws CustomBadCredentialsException;

    AuthenticationResponse refreshToken(
            RefreshTokenRequest request)
            throws InvalidRefreshTokenException, UserNotFoundException;

    // void logout();
}
