package com.mendeley.sdk;

/**
 * Data structure to wrap the client credentials needed to authenticate.
 */
public class ClientCredentials {
    public final String clientId;
    public final String clientSecret;
    public final String redirectUri;

    public ClientCredentials(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }
}
