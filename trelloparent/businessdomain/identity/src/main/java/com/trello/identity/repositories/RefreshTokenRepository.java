package com.trello.identity.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.identity.entities.RefreshToken;
import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // void deleteAllByUserId(UUID userId);
    @Modifying
    @Query("""
                DELETE FROM RefreshToken r
                WHERE r.expiresAt < :now
                   OR r.revokedAt IS NOT NULL
            """)
    void deleteExpiredOrRevokedRefreshTokens(@Param("now") LocalDateTime now);
}
