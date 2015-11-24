package com.mendeley.sdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utilities for the NetworkProviders.
 */
public class NetworkUtils {

    /**
     * Extracting json String from the given InputStream object.
     *
     * @param stream the InputStream holding the json string
     * @return the String
     * @throws IOException
     */
    public static String readInputStream(InputStream stream) throws IOException {
        StringBuffer data = new StringBuffer();
        InputStreamReader isReader = null;
        BufferedReader br = null;

        try {
            isReader = new InputStreamReader(stream);
            br = new BufferedReader(isReader);
            String brl = "";
            while ((brl = br.readLine()) != null) {
                data.append(brl);
            }

        } finally {
            stream.close();
            isReader.close();
            br.close();
        }

        return data.toString();
    }

    /**
     * Workaround method to allow unsupported HTTP methods (ie: PATCH) requests with {@link HttpURLConnection}
     *
     * {@see http://stackoverflow.com/questions/15209160/how-to-set-http-request-move-using-httpurlconnection}
     * {@see https://java.net/jira/browse/JERSEY-639}
     *
     * @param httpURLConnection
     * @param method
     */
    public static void setRequestMethodUsingWorkaroundForJREBug(final HttpURLConnection httpURLConnection, final String method) {
        try {
            httpURLConnection.setRequestMethod(method);
        } catch (final ProtocolException pe) {
            try {
                final Class<?> httpURLConnectionClass = httpURLConnection.getClass();
                final Class<?> parentClass = httpURLConnectionClass.getSuperclass();
                final Field methodField;
                // If the implementation class is an HTTPS URL Connection, we
                // need to go up one level higher in the heirarchy to modify the
                // 'method' field.
                if (parentClass == HttpsURLConnection.class) {
                    methodField = parentClass.getSuperclass().getDeclaredField("method");
                } else {
                    methodField = parentClass.getDeclaredField("method");
                }
                methodField.setAccessible(true);
                methodField.set(httpURLConnection, method);
            } catch (final Exception e) {
                throw new RuntimeException("Could not configure " + httpURLConnection + " to use " + method + " method", e);
            }
        }
    }
}
