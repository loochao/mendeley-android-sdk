package com.mendeley.api.impl;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.mendeley.api.auth.CredentialsManager;
import com.mendeley.api.util.Utils;

import java.util.Calendar;

/**
 * This class is responsible for storing and retrieving the credentials when needed.
 * Credentials are kept in SharedPreferences.
 */
public class SharedPreferencesCredentialsManager implements CredentialsManager {
	private static final String TAG = SharedPreferencesCredentialsManager.class.getSimpleName();
	
    // Shared preferences keys:
	private static final String ACCESS_TOKEN_KEY = "accessToken";
	private static final String REFRESH_TOKEN_KEY = "refreshToken";
	private static final String EXPIRES_AT_KEY = "expiresAt";
	private static final String TOKEN_TYPE_KEY = "tokenType";
 
	private SharedPreferences preferences;
 
	SharedPreferencesCredentialsManager(SharedPreferences preferences) {
        this.preferences = preferences;
	}

    @Override
    public void setCredentials(String accessToken, String refreshToken, String tokenType, int expiresIn)  {
        String expiresAt = generateExpiresAtFromExpiresIn(expiresIn);

        Editor editor = preferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.putString(TOKEN_TYPE_KEY, tokenType);
        editor.putString(EXPIRES_AT_KEY, expiresAt);
        editor.commit();
    }

    @Override
    public void clearCredentials() {
		Editor editor = preferences.edit();
		editor.remove(ACCESS_TOKEN_KEY);
		editor.remove(REFRESH_TOKEN_KEY);
		editor.remove(EXPIRES_AT_KEY);
		editor.remove(TOKEN_TYPE_KEY);
		editor.commit();
	}
 
    @Override
    public boolean hasCredentials() {
		return getAccessToken() != null;
	}

    @Override
    public String getExpiresAt() {
        return preferences.getString(EXPIRES_AT_KEY, null);
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

    public static String generateExpiresAtFromExpiresIn(int expiresIn) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, expiresIn);
        return Utils.dateFormat.format(c.getTime());
    }
}
