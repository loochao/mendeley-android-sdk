package com.mendeley.sdk.testUtils;

import android.content.res.AssetManager;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class UsernameAndPasswordSessionManagerFactory {

    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_USERNAME = "integration_test_username";
    private static final String KEY_PASSWORD = "integration_test_password";


    public static UsernameAndPasswordSessionManager create(AssetManager assetManager, ClientCredentials clientCredentials, AuthTokenManager authTokenManager) {
        try {
            InputStream is = assetManager.open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String username = propertyResourceBundle.getString(KEY_USERNAME);
            final String password = propertyResourceBundle.getString(KEY_PASSWORD);
            return new UsernameAndPasswordSessionManager(clientCredentials, authTokenManager, username, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
