package com.trello.identity.service;

import java.util.UUID;

import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;

public interface UserIdentityService {
    User findUserById(UUID userId) throws UserNotFoundException;

    User findUserByEmail(String email) throws UserNotFoundException;

    boolean existsUserByEmail(String email);

    User findUserByOtpTokenResetToken(UUID resetToken) throws UserNotFoundException;

    User saveUser(User user);
}
