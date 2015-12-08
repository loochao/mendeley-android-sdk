package com.mendeley.sdk.testUtils;

import android.content.res.AssetManager;

import com.mendeley.sdk.AppCredentials;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class ClientCredentialsFromAssetsFactory {

    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_PROJECT_ID = "integration_test_project_id";
    private static final String KEY_CLIENT_SECRET = "integration_test_client_secret";

    public static AppCredentials create(AssetManager assetManager) {
        try {
            InputStream is = assetManager.open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String clientId = propertyResourceBundle.getString(KEY_PROJECT_ID);
            final String clientSecret = propertyResourceBundle.getString(KEY_CLIENT_SECRET);

            return new AppCredentials(clientId, clientSecret);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
