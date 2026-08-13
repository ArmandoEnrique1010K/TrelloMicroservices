package com.trello.identity.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.identity.enums.OptPurpose;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "otpTokens")
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String optHash;
    private int attemps;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    private OptPurpose optPurpose;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
