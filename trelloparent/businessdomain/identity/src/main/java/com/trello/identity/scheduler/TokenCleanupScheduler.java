package com.trello.identity.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trello.identity.service.OtpTokenIdentityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final OtpTokenIdentityService otpTokenIdentityService;

    // Por cada 10 minutos va a eliminar los tokens en la base de datos
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void deleteExpiredValidatedTokens() {
        log.info("ELIMINANDO TODOS LOS TOKENS EXPIRADOS O UTILIZADOS");
        otpTokenIdentityService.deleteAllOtpTokensExpired();
    }
}
