package com.mendeley.api.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mendeley.api.R;
import com.mendeley.api.auth.AuthenticationManager;
import com.mendeley.api.impl.DefaultMendeleySdk;
import com.mendeley.api.network.NetworkUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity will show the login web interface in a webview.
 * The layout style will depend on the screen size. Full screen for small devices
 * and a dialog view for larg ones.
 */
public class SignInActivity extends Activity {
    private static final String OAUTH2_URL = NetworkUtils.API_URL + "oauth/authorize";

	private static final double SMALL_SCREEN_SIZE = 6.0;
	private static final String FORGOT_PASSWORD_URL = "http://www.mendeley.com/forgot/";
	private static final String TAG = SignInActivity.class.getSimpleName();

    private AuthenticationManager authenticationManager;
	private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticationManager = DefaultMendeleySdk.getInstance().getAuthenticationManager();

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
			webView.loadUrl(getOauth2URL(authenticationManager));
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
     * @param authenticationManager
	 */
	private static String getOauth2URL(AuthenticationManager authenticationManager) {
        StringBuilder urlString = new StringBuilder(OAUTH2_URL);
		
		urlString
		.append("?").append("grant_type=").append(AuthenticationManager.GRANT_TYPE_AUTH)
		.append("&").append("redirect_uri=").append(authenticationManager.getRedirectUri())
		.append("&").append("scope=").append(AuthenticationManager.SCOPE)
		.append("&").append("response_type=").append(AuthenticationManager.RESPONSE_TYPE)
		.append("&").append("client_id=").append(authenticationManager.getClientId());
		
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

    	protected String getJSONTokenString(String authorizationCode) throws IOException {
    		HttpResponse response = doPost(AuthenticationManager.TOKENS_URL, AuthenticationManager.GRANT_TYPE_AUTH,
                    authorizationCode, authenticationManager);
    		return NetworkUtils.readInputStream(response.getEntity().getContent());
		}

		protected String getAuthorizationCode(String authReturnUrl) {
    		String AuthorizationCode = null;
			int index = authReturnUrl.indexOf("code=");	       			
	        if (index != -1) {
	        	index += 5;
	        	AuthorizationCode = authReturnUrl.substring(index);
	        }

			return AuthorizationCode;
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			
			authorizationCode = getAuthorizationCode(params[0]);			
			if (authorizationCode != null) {
				try {
					String jsonTokenString = getJSONTokenString(authorizationCode);
                    authenticationManager.setTokenDetails(jsonTokenString);
                    result = "ok";
				} catch (IOException e) {
					Log.e(TAG, "", e);
				} catch (JSONException e) {
					Log.e(TAG, "", e);
				}
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Intent resultData = new Intent();

			if (authorizationCode != null) {
				authenticationManager.authenticated(true);
				setResult(Activity.RESULT_OK, resultData);
			} else {
				authenticationManager.failedToAuthenticate();
				setResult(Activity.RESULT_CANCELED, resultData);
			}

			finish();
		}
    }
    
	/**
	 * Helper method for executing http post request
	 * 
	 * @param url the url string
	 * @param grantType the grant type string
	 * @param authorizationCode the authorisation code string
	 * @return the HttpResponse object
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static HttpResponse doPost(String url, String grantType, String authorizationCode, AuthenticationManager authenticationManager)
            throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", grantType));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", authenticationManager.getRedirectUri()));
        nameValuePairs.add(new BasicNameValuePair("code", authorizationCode));
        nameValuePairs.add(new BasicNameValuePair("client_id", authenticationManager.getClientId()));
        nameValuePairs.add(new BasicNameValuePair("client_secret", authenticationManager.getClientSecret()));
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
		  
		return response;  
	}
}
