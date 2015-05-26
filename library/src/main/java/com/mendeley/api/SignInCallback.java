package com.mendeley.api;

/**
 * Interface that should be implemented by the application for receiving callbacks for sign
 * in events.
 */
public interface SignInCallback {
	public void onSignedIn();
    public void onSignInFailure();
}
