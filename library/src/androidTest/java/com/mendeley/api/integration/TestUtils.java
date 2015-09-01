package com.mendeley.api.integration;

import android.content.res.AssetManager;
import android.util.Log;

import com.mendeley.api.ClientCredentials;
import com.mendeley.api.callbacks.MendeleySignInInterface;
import com.mendeley.api.impl.InternalMendeleySdk;
import com.mendeley.api.testUtils.SignInException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    private static final String TAG = TestUtils.class.getSimpleName();

    private static final String CONFIG_FILE = "config.properties";

    private static final String KEY_USERNAME = "integration_test_username";
    private static final String KEY_PASSWORD = "integration_test_password";
    private static final String KEY_PROJECT_ID = "integration_test_project_id";
    private static final String KEY_CLIENT_SECRET = "integration_test_client_secret";
    private static final String KEY_CLIENT_REDIRECT_URI = "integration_test_client_redirect_url";

    private static final int SIGN_IN_TIMEOUT_MS = 10 * 1000;


    public static InternalMendeleySdk signIn(String username, String password, String clientId, String clientSecret, String redirectUri) throws InterruptedException, SignInException {
        final CountDownLatch signInLatch = new CountDownLatch(1);

        MendeleySignInInterface signinCallback = new MendeleySignInInterface() {
            @Override
            public void onSignedIn() {
                signInLatch.countDown();
            }

            @Override
            public void onSignInFailure() {}
        };


        ClientCredentials clientCredentials = new ClientCredentials(clientId, clientSecret, redirectUri);
        InternalMendeleySdk sdk = new InternalMendeleySdk(username, password, clientCredentials);
        sdk.signIn(null, signinCallback, true);

        if (!signInLatch.await(SIGN_IN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            throw new SignInException("timed out signing in");
        }
        return sdk;
    }

    public static InternalMendeleySdk signIn(AssetManager assetManager) throws SignInException, InterruptedException {
        try {
            InputStream is = assetManager.open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String username = propertyResourceBundle.getString(KEY_USERNAME);
            final String password = propertyResourceBundle.getString(KEY_PASSWORD);
            final String clientId = propertyResourceBundle.getString(KEY_PROJECT_ID);
            final String clientSecret = propertyResourceBundle.getString(KEY_CLIENT_SECRET);
            final String clientRedirectUri = propertyResourceBundle.getString(KEY_CLIENT_REDIRECT_URI);

            return signIn(username, password, clientId, clientSecret, clientRedirectUri);

        } catch (IOException ioe) {
            final String message = "Could not read property files with integration tests config. Should be located in assets/"+CONFIG_FILE;
            Log.e(TAG, message, ioe);
            throw new SignInException(message, ioe);
        } catch (MissingResourceException mr) {
            final String message = "Could not read property value from integration tests config. Check everything is configured in assets/"+CONFIG_FILE;
            Log.e(TAG, message, mr);
            throw new SignInException(message, mr);
        }
    }

    public static String getAssetsFileAsString(AssetManager assetManager, String fileName) throws IOException {
        StringBuilder buf=new StringBuilder();
        InputStream json=assetManager.open(fileName);
        BufferedReader in= new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();
        return buf.toString();
    }

}
