package com.trello.identity.service;

import java.util.Optional;
import java.util.UUID;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;

public interface IdentityService {
    User findUserById(UUID userId) throws UserNotFoundException;

    User findUserByEmail(String email) throws UserNotFoundException;

    boolean existsUserByEmail(String email);

    // Debe devolver un OptToken o un null si no existe el OtpToken por el ID del
    // usuario
    Optional<OtpToken> findOptionalOtpTokenByUserId(UUID userId);

    OtpToken findOtpTokenByUserId(UUID userId);

    User findUserByOtpTokenResetToken(UUID resetToken) throws UserNotFoundException;

    void deleteAllOtpTokensExpiredOrUsed();

    void deleteOtpTokenById(UUID otpTokenId);
}
