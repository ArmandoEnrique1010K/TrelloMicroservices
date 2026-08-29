package com.trello.identity.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.identity.repositories.RefreshTokenRepository;

@Service
public class RefreshTokenIdentityServiceImpl implements RefreshTokenIdentityService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenIdentityServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public void deleteAllRefreshTokensExpiredOrRevoked() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredOrRevokedRefreshTokens(now);
    }

}
