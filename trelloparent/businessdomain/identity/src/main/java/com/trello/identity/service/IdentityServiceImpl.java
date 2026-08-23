package com.trello.identity.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.identity.entities.OtpToken;
import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.repositories.OtpTokenRepository;
import com.trello.identity.repositories.UserRepository;

@Service
public class IdentityServiceImpl implements IdentityService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;

    public IdentityServiceImpl(UserRepository userRepository, OtpTokenRepository otpTokenRepository) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
    }

    @Override
    public User findUserById(UUID userId) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return user;
    }

    @Override
    public User findUserByEmail(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return user;
    }

    // No se utiliza una excepcion para una expresion booleana
    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<OtpToken> findOptionalOtpTokenByUserId(UUID userId) {
        Optional<OtpToken> otpToken = otpTokenRepository.findByUserId(userId);
        return otpToken;
    }

    @Override
    public OtpToken findOtpTokenByUserId(UUID userId) {
        OtpToken otpToken = otpTokenRepository.findByUserId(userId).get();
        return otpToken;
    }
}
