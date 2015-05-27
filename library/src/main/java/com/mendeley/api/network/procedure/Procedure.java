package com.mendeley.api.network.procedure;

import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.exceptions.HttpResponseException;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.exceptions.NotSignedInException;

public abstract class Procedure<Result> {
    protected AuthenticationManager authenticationManager;

    public Procedure(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    protected abstract Result run() throws MendeleyException;

    public Result checkedRun() throws MendeleyException {
        if (authenticationManager == null || !authenticationManager.isSignedIn()) {
            // Must call startSignInProcess first - caller error!
            throw new NotSignedInException();
        }
        if (authenticationManager.willExpireSoon()) {
            authenticationManager.refreshToken();
        }
        try {
            return run();
        } catch (HttpResponseException e) {
            if (e.httpReturnCode == 401 && e.getMessage().contains("Token has expired")) {
                // The refresh-token-in-advance logic did not work for some reason: force a refresh now
                authenticationManager.refreshToken();
                return run();
            } else {
                throw e;
            }
        }
    }
}
