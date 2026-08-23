package com.trello.identity.token.service;

import java.util.UUID;

import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.token.dto.request.EmailRequest;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.TokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.exception.InvalidTokenException;

public interface TokenService {
    void sendConfirmAccountToken(UUID userId) throws UserNotFoundException;

    void validateConfirmAccountToken(UUID userId, TokenRequest tokenRequest)
            throws InvalidTokenException, UserNotFoundException;

    void sendPasswordResetToken(EmailRequest emailRequest);

    ValidatePasswordResetTokenResponse validatePasswordResetToken(TokenRequest tokenRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
