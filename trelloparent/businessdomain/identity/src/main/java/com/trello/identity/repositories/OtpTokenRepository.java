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

    /*
     * Ejecuta directamente una operación DELETE sobre la base de datos.
     *
     * @Modifying indica a Spring Data JPA que la consulta modifica datos
     * en lugar de realizar una consulta de lectura.
     *
     * Devuelve la cantidad de registros afectados:
     * 1 -> token eliminado
     * 0 -> no se encontró el token
     */
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.id = :id")
    int deleteTokenById(@Param("id") UUID id);
}
