package com.mendeley.sdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}
