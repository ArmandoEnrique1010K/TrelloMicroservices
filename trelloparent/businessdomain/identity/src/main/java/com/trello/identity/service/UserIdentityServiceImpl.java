package com.trello.identity.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.repositories.UserRepository;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {
    private final UserRepository userRepository;

    public UserIdentityServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public User findUserByOtpTokenResetToken(UUID resetToken) throws UserNotFoundException {
        User user = userRepository.findByOtpTokenResetToken(resetToken).orElseThrow(UserNotFoundException::new);
        return user;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
