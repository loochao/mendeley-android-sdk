package com.mendeley.api.impl;

import android.content.SharedPreferences;

import com.mendeley.api.AuthTokenManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Adds async calls to BaseMendeleySdk.
 */
class SharedPreferencesAuthTokenManager implements AuthTokenManager {


    // Shared preferences keys:
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String EXPIRES_AT_KEY = "expiresAtDate";
    private static final String TOKEN_TYPE_KEY = "tokenType";

    private final SharedPreferences preferences;

    SharedPreferencesAuthTokenManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn)  {
        Date expiresAt = generateExpiresAtFromExpiresIn(expiresIn);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.putString(TOKEN_TYPE_KEY, tokenType);
        editor.putLong(EXPIRES_AT_KEY, expiresAt.getTime());
        editor.commit();
    }

    @Override
    public void clearTokens() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.remove(REFRESH_TOKEN_KEY);
        editor.remove(EXPIRES_AT_KEY);
        editor.remove(TOKEN_TYPE_KEY);
        editor.commit();
    }

    @Override
    public Date getAuthTenExpiresAt() {
        return new Date(preferences.getLong(EXPIRES_AT_KEY, 0));
    }

    @Override
    public String getRefreshToken() {
        return preferences.getString(REFRESH_TOKEN_KEY, null);
    }

    /**
     * @return the access token string, or null if it does not exist.
     */
    @Override
    public String getAccessToken() {
        return preferences.getString(ACCESS_TOKEN_KEY, null);
    }

    @Override
    public String getTokenType() {
        return preferences.getString(TOKEN_TYPE_KEY, null);
    }

    private Date generateExpiresAtFromExpiresIn(int expiresIn) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, expiresIn);
        return c.getTime();
    }


}
