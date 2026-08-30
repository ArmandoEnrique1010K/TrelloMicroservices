package com.trello.project.entities;

import java.util.UUID;

import com.trello.project.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;

    // ID del usuario
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private Role role;
}
