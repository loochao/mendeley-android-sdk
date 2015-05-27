package com.mendeley.api.impl;

import android.app.Activity;
import android.content.Intent;

import com.mendeley.api.ClientCredentials;
import com.mendeley.api.activity.SignInActivity;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.callbacks.MendeleySignInInterface;

public class DefaultMendeleySdk extends AsyncMendeleySdk {
    private static DefaultMendeleySdk instance;

    /**
     * Return the MendeleySdk singleton.
     */
    public static DefaultMendeleySdk getInstance() {
        if (instance == null) {
            instance = new DefaultMendeleySdk();
        }
        return instance;
    }

    private DefaultMendeleySdk() {
    }

    @Override
    public void signIn(Activity activity, MendeleySignInInterface signInCallback,
                       ClientCredentials clientCredentials) {
        this.mendeleySignInInterface = signInCallback;
        authenticationManager = new AuthenticationManager(
                activity,
                createAuthenticationInterface(),
                clientCredentials.clientId,
                clientCredentials.clientSecret,
                clientCredentials.redirectUri);
        initProviders();

        if (authenticationManager.isSignedIn()) {
            return;
        }
        final Intent intent = new Intent(activity, SignInActivity.class);
        activity.startActivity(intent);
    }
}
