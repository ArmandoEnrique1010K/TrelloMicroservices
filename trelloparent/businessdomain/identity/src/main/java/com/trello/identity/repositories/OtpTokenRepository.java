package com.trello.identity.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.identity.entities.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findByUserId(UUID userId);

    Optional<OtpToken> findByUserEmail(String email);
}
