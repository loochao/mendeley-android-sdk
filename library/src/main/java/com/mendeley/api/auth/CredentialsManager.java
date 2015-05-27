package com.mendeley.api.auth;

public interface CredentialsManager {

    /**
     * Stores the token details in shared preferences.
     *
     * @param accessToken the access toekn string
     * @param refreshToken the refresh token string
     * @param tokenType the token type string
     * @param expiresIn the expires in value
     */
    void setCredentials(String accessToken, String refreshToken, String tokenType, int expiresIn);

    /**
     * Removes the credentials from the SharedPreferences as well as the NetworkProvider static string objects.
     */
    void clearCredentials();

    /**
     * Check if required credentials exist.
     */
    boolean hasCredentials();

    /**
     * @return the expires in string value or null if it does not exist.
     */
    String getExpiresAt();

    /**
     * @return the refresh token string or null if it does not exist.
     */
    String getRefreshToken();

    /**
     * @return the access token string or null if it does not exist.
     */
    String getAccessToken();
}
