package com.trello.project.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trello.project.service.InvitationProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationCleanupScheduler {
    private final InvitationProjectService invitationProjectService;

    // Por cada semana se van a eliminar las invitaciones aceptadas
    @Scheduled(fixedRate = 7, timeUnit = TimeUnit.DAYS)
    public void deleteAcceptedInvitations() {
        log.info("ELIMINANDO TODAS LAS INVITACIONES ACEPTADAS");

        invitationProjectService.deleteAllAcceptedInvitations();
    }
}
