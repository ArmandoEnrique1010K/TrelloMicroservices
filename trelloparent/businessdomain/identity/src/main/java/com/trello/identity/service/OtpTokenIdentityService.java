package com.trello.identity.service;

import java.util.Optional;
import java.util.UUID;

import com.trello.identity.entities.OtpToken;

public interface OtpTokenIdentityService {
    // Debe devolver un OptToken o un null si no existe el OtpToken por el ID del
    // usuario
    Optional<OtpToken> findOptionalOtpTokenByUserId(UUID userId);

    void deleteOtpTokenById(UUID otpTokenId);

    void deleteAllOtpTokensExpiredOrUsed();

    void saveOtpToken(OtpToken otpToken);
}
