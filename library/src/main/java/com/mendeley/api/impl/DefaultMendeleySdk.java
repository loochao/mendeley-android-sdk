package com.mendeley.api.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mendeley.api.ClientCredentials;
import com.mendeley.api.activity.SignInActivity;
import com.mendeley.api.activity.SignInOrSignUpActivity;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.auth.CredentialsManager;
import com.mendeley.api.callbacks.MendeleySignInInterface;

public class DefaultMendeleySdk extends AsyncMendeleySdk {
    private static DefaultMendeleySdk instance;

    public static void sdkInitialise(Application applicationContext, ClientCredentials clientCredentials) {
        instance = new DefaultMendeleySdk(applicationContext, clientCredentials);
    }

    /**
     * Return the MendeleySdk singleton.
     */
    public static DefaultMendeleySdk getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Sdk is not initialised. You must call #sdkInitialise() first.");
        }
        return instance;
    }

    private DefaultMendeleySdk(Context context, ClientCredentials clientCredentials) {
        final SharedPreferences preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        CredentialsManager credentialsManager = new SharedPreferencesCredentialsManager(preferences);

        authenticationManager = new AuthenticationManager(
                credentialsManager,
                createAuthenticationInterface(),
                clientCredentials.clientId,
                clientCredentials.clientSecret,
                clientCredentials.redirectUri);

        initProviders();
    }

    @Override
    public void signIn(Activity activity, MendeleySignInInterface signInCallback, boolean showSignUpScreen) {
        this.mendeleySignInInterface = signInCallback;

        final Intent intent;
        if (showSignUpScreen) {
            if (authenticationManager.isSignedIn()) {
                mendeleySignInInterface.onSignedIn();
                return;
            }
            intent = new Intent(activity, SignInOrSignUpActivity.class);
        } else {
            intent = new Intent(activity, SignInActivity.class);
        }
        activity.startActivity(intent);
    }

}
