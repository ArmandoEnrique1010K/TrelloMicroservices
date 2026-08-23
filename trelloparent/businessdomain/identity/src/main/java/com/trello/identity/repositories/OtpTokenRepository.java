package com.trello.identity.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.identity.entities.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findByUserId(UUID userId);

    Optional<OtpToken> findByUserEmail(String email);

    @Modifying
    @Query("""
                DELETE FROM OtpToken o
                WHERE o.expiresAt <= :now
                   OR o.usedAt IS NOT NULL
            """)
    void deleteExpiredOrUsedOtpTokens(@Param("now") LocalDateTime now);
}
