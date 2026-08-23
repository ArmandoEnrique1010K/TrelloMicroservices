package com.trello.identity.profile.service;

import java.util.UUID;

import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.request.CheckPasswordRequest;
import com.trello.identity.profile.dto.request.UpdatePasswordRequest;
import com.trello.identity.profile.dto.response.ProfileResponse;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.profile.exception.MismatchSameOldPasswordException;
import com.trello.identity.profile.exception.MismatchUpdatePasswordException;

public interface ProfileService {
    ProfileResponse getProfile(UUID userId) throws UserNotFoundException;

    void checkPassword(UUID userId, CheckPasswordRequest checkPasswordRequest) throws UserNotFoundException,
            MismatchCheckPasswordException;

    void updatePassword(UUID userId,
            UpdatePasswordRequest updatePasswordRequest)
            throws UserNotFoundException, MismatchCheckPasswordException, MismatchUpdatePasswordException,
            MismatchSameOldPasswordException;
}
