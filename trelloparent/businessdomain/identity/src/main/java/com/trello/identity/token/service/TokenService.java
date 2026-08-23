package com.trello.identity.token.service;

import java.util.UUID;

import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.token.dto.request.SendPasswordResetTokenRequest;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.ValidateConfirmAccountTokenRequest;
import com.trello.identity.token.dto.request.ValidatePasswordResetTokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.exception.ConfirmedAccountException;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.exception.UnconfirmedAccountException;

public interface TokenService {
    void sendConfirmAccountToken(UUID userId) throws UserNotFoundException, ConfirmedAccountException;

    void validateConfirmAccountToken(UUID userId, ValidateConfirmAccountTokenRequest validateConfirmAccountTokenRequest)
            throws InvalidTokenException, UserNotFoundException, ConfirmedAccountException;

    void sendPasswordResetToken(SendPasswordResetTokenRequest sendPasswordResetTokenRequest)
            throws UserNotFoundException, UnconfirmedAccountException;

    ValidatePasswordResetTokenResponse validatePasswordResetToken(
            ValidatePasswordResetTokenRequest validatePasswordResetTokenRequest)
            throws InvalidTokenException, UserNotFoundException, UnconfirmedAccountException;

    // Reestablece la contraseña si no recuerda su contraseña anterior
    void resetPassword(ResetPasswordRequest resetPasswordRequest) throws UserNotFoundException,
            UnconfirmedAccountException, MismatchUpdatePasswordException, MismatchSameOldPasswordException;
}
