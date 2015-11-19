package com.mendeley.api.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.mendeley.api.R;

/**
 * Display the screen to sign in or sign up, with buttons to sign in or create an account.
 *
 * The "create account" button starts a new web browser activity pointed at the
 * Mendeley home page.
 *
 * The "sign in" button starts SignInActivity to obtain an authorization code, which is
 * passed to AuthenticationApi.
 */
public class SignInOrSignUpActivity extends Activity implements OnClickListener {
	private final static String CREATE_ACCOUNT_URL = "http://www.mendeley.com/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash_layout);

        findViewById(R.id.signinButton).setOnClickListener(this);
        findViewById(R.id.signupButton).setOnClickListener(this);
    }

    /**
     * Handling click events on the custom buttons.
     * Will call SignInActivity or open the create account url in a web browser.
     */
	@Override
	public void onClick(View v) {
		if (!isOnline(this)) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
			return;
		}

		int id = v.getId();
		if (id == R.id.signinButton) {
			Intent intent = new Intent(this, SignInActivity.class);
			startActivityForResult(intent, SignInActivity.AUTH_REQUEST_CODE);
		} else if (id == R.id.signupButton) {
			openUrlInBrowser(CREATE_ACCOUNT_URL);
		}
	}

    /**
     * Opening a system web browser to load the given url
     * @param url the url to load
     */
    private void openUrlInBrowser(String url) {
    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    	startActivity(intent);
    }

    /**
     * Handling on activity result.
     * Will be called after the user logged in in the SignInActivity.
     * If an authorisation code is received will call authenticated method of the AuthenticationApi
     * otherwise will call failedToAuthenticate.
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case SignInActivity.AUTH_REQUEST_CODE:
				setResult(resultCode, data);
				finish();
				break;
    		}
    }

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	private boolean isOnline(Context context) {
		final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected();
	}
}