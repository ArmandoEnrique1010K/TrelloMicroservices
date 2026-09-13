package com.trello.identity.user.service;

import com.trello.identity.user.dto.response.UserResponse;
import com.trello.identity.user.mapper.UserResponseMapperImpl;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.identity.entities.User;
import com.trello.identity.service.UserIdentityService;

@Service
public class UserServiceImpl implements UserService {

    private final UserResponseMapperImpl userResponseMapperImpl;
    private final UserIdentityService userIdentityService;

    // Proveedores genericos de email
    private static final Set<String> IGNORED_EMAIL_PROVIDERS = Set.of(
            "gmail",
            "hotmail",
            "outlook");

    public UserServiceImpl(UserIdentityService userIdentityService, UserResponseMapperImpl userResponseMapperImpl) {
        this.userIdentityService = userIdentityService;
        this.userResponseMapperImpl = userResponseMapperImpl;
    }

    @Override
    public List<UserResponse> listAllUsersByEmailExcludingIds(String email, List<UUID> ids) {

        String normalizedEmail = email.trim().toLowerCase();

        // No realizar la busqueda si hay menos de 6 caracteres o si esta buscando por
        // dominio de email
        if (normalizedEmail.length() < 6 ||
                isGenericEmailSearch(normalizedEmail)) {
            return List.of();
        }

        List<User> listUsers = userIdentityService.findAllUsersByKeywordEmailAndExcludingIds(email, ids);

        return userResponseMapperImpl.userListToUserResponseList(listUsers);
    }

    // Verifica si esta realizando una busqueda por dominio de email
    private boolean isGenericEmailSearch(String email) {

        String normalizedEmail = email
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z]", "");

        return IGNORED_EMAIL_PROVIDERS.stream()
                .anyMatch(provider -> normalizedEmail.startsWith(provider));
    }
}
