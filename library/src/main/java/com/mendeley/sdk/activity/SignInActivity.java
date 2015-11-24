package com.mendeley.sdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.Mendeley;
import com.mendeley.sdk.R;
import com.mendeley.sdk.exceptions.HttpResponseException;
import com.mendeley.sdk.request.Request;
import com.mendeley.sdk.util.NetworkUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * This activity will show the login web interface in a webview.
 * The layout style will depend on the screen size. Full screen for small devices
 * and a dialog view for larg ones.
 */
public class SignInActivity extends Activity {

	private final static String GRANT_TYPE_AUTH = "authorization_code";
	private final static String SCOPE = "all";
	private final static String RESPONSE_TYPE = "code";

	public final static int AUTH_REQUEST_CODE = 31231;
	public static final String EXTRA_JSON_TOKENS = "returned_json_tokens";

    private static final String OAUTH2_URL = Request.MENDELEY_API_BASE_URL + "oauth/authorize";

	private static final double SMALL_SCREEN_SIZE = 6.0;
	private static final String FORGOT_PASSWORD_URL = "http://www.mendeley.com/forgot/";
	private static final String TAG = SignInActivity.class.getSimpleName();

	private WebView webView;

	private Mendeley mendeley;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mendeley = Mendeley.getInstance();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getScreenSize() <= SMALL_SCREEN_SIZE) {
			super.setTheme(R.style.AppTheme);

        } else {
			setFinishOnTouchOutside(false);
		}

        setContentView(R.layout.dialog_layout);
		webView = (WebView) findViewById(R.id.dialogWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setVerticalScrollBarEnabled(true);
		webView.setHorizontalScrollBarEnabled(true);
		webView.setVerticalScrollBarEnabled(true);
		webView.setHorizontalScrollBarEnabled(true);
		webView.requestFocusFromTouch();
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setUserAgentString("Android " + getPackageName());
		webView.setWebViewClient(new MendeleyWebViewClient());

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(getOauth2URL());
		}

        View dismissButton = findViewById(R.id.dismissButton);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
	}

	/**
     * Finding the screen size in inches
     *
     * @return the screen size
     */
    private double getScreenSize() {
    	DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        double screenInches = Math.sqrt(x+y);
        screenInches =  (double)Math.round(screenInches * 10) / 10;

        return screenInches;
    }

	/**
	 * Creating and return the Oauth2 url string.
	 *
	 * @return the url string
	 */
	private String getOauth2URL() {
        StringBuilder urlString = new StringBuilder(OAUTH2_URL);

		urlString
		.append("?").append("grant_type=").append(GRANT_TYPE_AUTH)
		.append("&").append("redirect_uri=").append(mendeley.getClientCredentials().redirectUri)
		.append("&").append("scope=").append(SCOPE)
		.append("&").append("response_type=").append(RESPONSE_TYPE)
		.append("&").append("client_id=").append(mendeley.getClientCredentials().clientId);

		return urlString.toString();
	}

	/**
	 * A WebViewClient that starts the AuthenticationTask when a new url is loaded.
	 */
    private class MendeleyWebViewClient extends WebViewClient {
    	@Override
    	public boolean shouldOverrideUrlLoading (WebView view, String url) {

    		if (url.equals(FORGOT_PASSWORD_URL)) {
    			openUrlInBrowser(url);
    		} else {
    			new AuthenticateTask().execute(url);
    		}
			return true;
    	}
    }

    /**
     * Opening a web browser to load the given url
     * @param url the url to load
     */
    private void openUrlInBrowser(String url) {
    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    	startActivity(intent);
    }

    /**
	 * AsyncTask class that carry out the authentication task and send the
	 * authorisation code with the result data, which will be received by
	 * the SignInOrSignUpActivity
	 */
    private final class AuthenticateTask extends AsyncTask<String, Void, String> {
        private String authorizationCode;

		protected String getAuthorizationCode(String authReturnUrl) {
    		String authorizationCode = null;
			int index = authReturnUrl.indexOf("code=");
	        if (index != -1) {
	        	index += 5;
	        	authorizationCode = authReturnUrl.substring(index);
	        }

			return authorizationCode;
    	}

		@Override
		protected String doInBackground(String... params) {
			String authReturnUrl = params[0];
			authorizationCode = getAuthorizationCode(authReturnUrl);

			if (authorizationCode != null) {
				try {
					return postAuthorizationCode(AuthTokenManager.TOKENS_URL, GRANT_TYPE_AUTH, authorizationCode);
				} catch (Exception e) {
					Log.e(TAG, "Could not obtain the access token from authorization code", e);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String jsonTokenString) {
			if (!TextUtils.isEmpty(jsonTokenString)) {
				setResult(Activity.RESULT_OK, new Intent().putExtra(SignInActivity.EXTRA_JSON_TOKENS, jsonTokenString));
			} else {
				setResult(Activity.RESULT_CANCELED, new Intent());
			}
			finish();
		}
    }
    
	/**
	 * Helper method for executing http post request
	 */
	private String postAuthorizationCode(String url, String grantType, String authorizationCode)  throws Exception {

		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) new URL(url).openConnection();
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoInput(true);
			con.setDoOutput(true);

			final String urlEncodedForm = new Uri.Builder()
					.appendQueryParameter("grant_type", grantType)
					.appendQueryParameter("redirect_uri", mendeley.getClientCredentials().redirectUri)
					.appendQueryParameter("code", authorizationCode)
					.appendQueryParameter("client_id", mendeley.getClientCredentials().clientId)
					.appendQueryParameter("client_secret", mendeley.getClientCredentials().clientSecret)
					.build()
					.getEncodedQuery();

			//Send request
			DataOutputStream wr = new DataOutputStream(con.getOutputStream ());
			wr.writeBytes (urlEncodedForm);
			wr.flush ();
			wr.close ();

			final int statusCode = con.getResponseCode();

			if (statusCode != 200) {
				String responseBody = "";
				try {
					return NetworkUtils.readInputStream(con.getInputStream());
				} catch (IOException ignored) {
				}
				throw new HttpResponseException(statusCode, con.getResponseMessage(), url, responseBody);
			}

			return NetworkUtils.readInputStream(con.getInputStream());

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}


}
