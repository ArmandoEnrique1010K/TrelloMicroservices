package com.trello.identity.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.repositories.OtpTokenRepository;

@Service
public class OtpTokenIdentityServiceImpl implements OtpTokenIdentityService {
    private final OtpTokenRepository otpTokenRepository;

    public OtpTokenIdentityServiceImpl(OtpTokenRepository otpTokenRepository) {
        this.otpTokenRepository = otpTokenRepository;
    }

    @Override
    public Optional<OtpToken> findOptionalOtpTokenByUserId(UUID userId) {
        Optional<OtpToken> otpToken = otpTokenRepository.findByUserId(userId);
        return otpToken;
    }

    @Override
    public void deleteOtpTokenById(UUID otpTokenId) {
        otpTokenRepository.deleteTokenById(otpTokenId);
    }

    @Override
    // Como este método se va a ejecutar automaticamente cada cierto tiempo debe
    // tener un @Transactional
    @Transactional
    public void deleteAllOtpTokensExpiredOrUsed() {
        LocalDateTime now = LocalDateTime.now();
        otpTokenRepository.deleteExpiredOrUsedOtpTokens(now);
    }

    @Override
    public void saveOtpToken(OtpToken otpToken) {
        otpTokenRepository.save(otpToken);
    }

}
