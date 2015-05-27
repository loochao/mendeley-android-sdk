package com.mendeley.api.impl;

import com.mendeley.api.auth.CredentialsManager;
import com.mendeley.api.auth.SharedPreferencesCredentialsManager;

public class InMemoryCredentialsManager implements CredentialsManager {
    private String accessToken; // null if not set
    private String refreshToken;
    private String expiresAt;

    @Override
    public void setCredentials(String accessToken, String refreshToken, String tokenType, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;

        expiresAt = SharedPreferencesCredentialsManager.generateExpiresAtFromExpiresIn(expiresIn);
    }

    @Override
    public void clearCredentials() {
        accessToken = null;
    }

    @Override
    public boolean hasCredentials() {
        return accessToken != null;
    }

    @Override
    public String getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }
}
