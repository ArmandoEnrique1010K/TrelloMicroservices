package com.trello.identity.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.identity.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);
}
