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

package com.mendeley.api;

import com.mendeley.api.request.Request;

import java.util.Date;

/**
 * Main entry points for making calls to the Mendeley SDK.
 */
public interface AuthTokenManager {

    String TOKENS_URL = Request.API_URL + "/oauth/token";
    String GRANT_TYPE_AUTH = "authorization_code";
    String GRANT_TYPE_REFRESH = "refresh_token";
    String SCOPE = "all";
    String RESPONSE_TYPE = "code";

    // Only use tokens which don't expire in the next 5 mins:
    int MIN_TOKEN_VALIDITY_SEC = 300;


    String getAccessToken();

    String getRefreshToken();

    Date getAuthTenExpiresAt();

    String getTokenType();

    /**
     * Stores the token details in shared preferences.
     *
     * @param accessToken the access toekn string
     * @param refreshToken the refresh token string
     * @param tokenType the token type string
     * @param expiresIn the expires in value
     */
    void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn);

    void clearTokens();
}
