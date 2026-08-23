package com.trello.identity.profile.service;

import java.util.UUID;

import com.trello.identity.exception.MismatchSameOldPasswordException;
import com.trello.identity.exception.MismatchUpdatePasswordException;
import com.trello.identity.exception.UserNotFoundException;
import com.trello.identity.profile.dto.request.CheckPasswordRequest;
import com.trello.identity.profile.dto.request.UpdatePasswordRequest;
import com.trello.identity.profile.dto.response.ProfileResponse;
import com.trello.identity.profile.exception.MismatchCheckPasswordException;
import com.trello.identity.token.exception.UnconfirmedAccountException;

public interface ProfileService {
    ProfileResponse getProfile(UUID userId) throws UserNotFoundException, UnconfirmedAccountException;

    void checkPassword(UUID userId, CheckPasswordRequest checkPasswordRequest) throws UserNotFoundException,
            UnconfirmedAccountException,
            MismatchCheckPasswordException;

    void updatePassword(UUID userId,
            UpdatePasswordRequest updatePasswordRequest)
            throws UserNotFoundException, UnconfirmedAccountException, MismatchCheckPasswordException,
            MismatchUpdatePasswordException,
            MismatchSameOldPasswordException;
}
