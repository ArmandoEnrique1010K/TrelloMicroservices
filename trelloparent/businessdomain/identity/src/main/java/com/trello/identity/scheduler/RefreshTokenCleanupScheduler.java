package com.trello.identity.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trello.identity.service.RefreshTokenIdentityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {
    private final RefreshTokenIdentityService refreshTokenIdentityService;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void deleteExpiredValidatedTokens() {
        log.info("ELIMINANDO TODOS LOS TOKENS DE REVALIDACIÓN EXPIRADOS O REVOCADOS");
        refreshTokenIdentityService.deleteAllRefreshTokensExpiredOrRevoked();
    }
}
