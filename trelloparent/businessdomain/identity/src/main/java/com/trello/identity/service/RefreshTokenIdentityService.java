package com.trello.identity.service;

public interface RefreshTokenIdentityService {
    void deleteAllRefreshTokensExpiredOrRevoked();
}
