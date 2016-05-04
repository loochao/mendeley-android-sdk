package com.mendeley.sdk.testUtils;

import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class EmailAndPasswordFromAssetsFactory {

    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_USERNAME = "integration_test_username";
    private static final String KEY_PASSWORD = "integration_test_password";


    public static UsernameAndPassword create(AssetManager assetManager) {
        try {
            InputStream is = assetManager.open(CONFIG_FILE);
            InputStream bis = new BufferedInputStream(is);
            Reader reader = new InputStreamReader(bis);
            ResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);

            final String username = propertyResourceBundle.getString(KEY_USERNAME);
            final String password = propertyResourceBundle.getString(KEY_PASSWORD);
            return new UsernameAndPassword(username, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class UsernameAndPassword {
        public final String username;
        public final String password;

        private UsernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }


}
