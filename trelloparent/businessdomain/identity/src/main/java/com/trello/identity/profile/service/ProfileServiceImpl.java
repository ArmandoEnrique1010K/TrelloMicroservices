package com.trello.identity.profile.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trello.identity.entities.User;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.request.CheckPasswordRequest;
import com.trello.identity.profile.dto.request.UpdatePasswordRequest;
import com.trello.identity.profile.dto.response.ProfileResponse;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.profile.exception.MismatchSameOldPasswordException;
import com.trello.identity.profile.exception.MismatchUpdatePasswordException;
import com.trello.identity.profile.mapper.ProfileResponseMapper;
import com.trello.identity.repositories.UserRepository;
import com.trello.identity.service.IdentityService;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final IdentityService identityService;
    private final ProfileResponseMapper profileResponseMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(
            UserRepository userRepository,
            IdentityService identityService, ProfileResponseMapper profileResponseMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.identityService = identityService;
        this.profileResponseMapper = profileResponseMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProfileResponse getProfile(UUID userId) throws UserNotFoundException {
        User existingUser = identityService.findUserById(userId);
        ProfileResponse profileResponseToUser = profileResponseMapper.userToProfileResponse(existingUser);
        return profileResponseToUser;
    }

    @Override
    public void checkPassword(UUID userId, CheckPasswordRequest checkPasswordRequest)
            throws UserNotFoundException, MismatchCheckPasswordException {
        User existingUser = identityService.findUserById(userId);

        if (!passwordEncoder.matches(
                checkPasswordRequest.getCurrentPassword(),
                existingUser.getPassword())) {

            throw new MismatchCheckPasswordException();
        }
    }

    @Override
    public void updatePassword(UUID userId, UpdatePasswordRequest updatePasswordRequest)
            throws UserNotFoundException, MismatchCheckPasswordException, MismatchUpdatePasswordException,
            MismatchSameOldPasswordException {
        User existingUser = identityService.findUserById(userId);

        if (!passwordEncoder.matches(
                updatePasswordRequest.getCurrentPassword(),
                existingUser.getPassword())) {
            throw new MismatchCheckPasswordException();
        }

        if (!updatePasswordRequest.getNewPassword()
                .equals(updatePasswordRequest.getNewPasswordConfirmation())) {
            throw new MismatchUpdatePasswordException();
        }

        if (passwordEncoder.matches(existingUser.getPassword(), updatePasswordRequest.getNewPassword())) {
            throw new MismatchSameOldPasswordException();
        }

        existingUser.setPassword(
                passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        userRepository.save(existingUser);
    }
}
