package com.trello.identity.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.identity.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByOtpTokenResetToken(UUID resetToken);

    // Listar por email (palabra clave), confirmed = true y excluyendo los IDs los
    // primeros 10 por orden descendente por fecha de creación
    List<User> findTop10ByEmailContainingIgnoreCaseAndIdNotInAndConfirmedTrueOrderByCreatedAtDesc(String keyword,
            List<UUID> excludedIds);
}
