/*
 * Copyright 2014 Mendeley Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mendeley.sdk;

import com.mendeley.sdk.request.Request;

import java.util.Date;

/**
 *  Manager used by the {@link Request} to get the OAuth access and refresh tokens from in order
 *  to launch HTTP requests against the Mendeley API.
 *
 *  @{see http://dev.mendeley.com/}
 */
public interface AuthTokenManager {

    String TOKENS_URL = Request.MENDELEY_API_BASE_URL + "/oauth/token";
    String REDIRECT_URI = "http://localhost/auth_return";

    /**
     * @return the OAuth access token.
     */
    String getAccessToken();

    /**
     * @return the OAuth request token.
     */
    String getRefreshToken();

    /**
     * @return a date with the expiration of the access token
     */
    Date getAuthTokenExpirationDate();

    /**
     * @return the type of the token
     */
    String getTokenType();

    /**
     * Stores the tokens for further retrieval.
     * Implementations of the interface should decide if they will use a persistent method for
     * storing the data or any other approach.
     *
     * @param accessToken the access toekn string
     * @param refreshToken the refresh token string
     * @param tokenType the token type string
     * @param expiresIn the expires in value
     */
    void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn);

    /**
     * Clears the tokens.
     * This implies logging the user out from the Mendeley API.
     * After this method is invoked, the user will need to sign in again or {@link Request}s will
     * end up in a authorization error.
     */
    void clearTokens();
}
