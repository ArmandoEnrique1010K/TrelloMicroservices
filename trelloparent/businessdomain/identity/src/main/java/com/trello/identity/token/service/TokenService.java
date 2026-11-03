package com.trello.identity.token.service;

import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.token.dto.request.SendTokenRequest;
import com.trello.identity.token.dto.request.ResetPasswordRequest;
import com.trello.identity.token.dto.request.ValidateTokenRequest;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;
import com.trello.identity.token.exception.ConfirmedAccountException;
import com.trello.identity.token.exception.InvalidTokenException;
import com.trello.identity.token.exception.UnconfirmedAccountException;

public interface TokenService {
    void sendConfirmAccountToken(
            SendTokenRequest sendTokenRequest)
            throws UserNotFoundException, ConfirmedAccountException;

    void validateConfirmAccountToken(
            ValidateTokenRequest validateTokenRequest)
            throws UserNotFoundException, ConfirmedAccountException, InvalidTokenException;

    void sendPasswordResetToken(SendTokenRequest sendTokenRequest)
            throws UserNotFoundException, UnconfirmedAccountException;

    ValidatePasswordResetTokenResponse validatePasswordResetToken(
            ValidateTokenRequest validateTokenRequest)
            throws UserNotFoundException, InvalidTokenException, UnconfirmedAccountException;

    // Reestablece la contraseña si no recuerda su contraseña anterior
    void resetPassword(ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, UnconfirmedAccountException, MismatchUpdatePasswordException,
            MismatchSameOldPasswordException;
}
