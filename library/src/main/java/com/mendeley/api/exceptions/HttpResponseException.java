package com.mendeley.api.exceptions;

import com.mendeley.api.request.NetworkUtils;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * General exception type that is thrown whenever server returns an error code.
 * <p>
 * This is typically due to invalid parameters (httpReturnCode 4xx),
 * but could also be an internal error (httpReturnCode 5xx).
 */
public class HttpResponseException extends MendeleyException {

    public final String url;
    public final int httpReturnCode;
    private final String response;

    public static HttpResponseException create(HttpURLConnection con) {
        try {
            final int responseCode = con.getResponseCode();
            final String responseMessage = con.getResponseMessage();
            final String url = con.getURL().toString();
            final InputStream is = con.getErrorStream();
            return create(responseCode, responseMessage, url, is);
        } catch (IOException e) {
            return new HttpResponseException(-1, "Unknown", "Unknown");
        }
    }

    public static HttpResponseException create(HttpResponse response, String url) {
        final int responseCode = response.getStatusLine().getStatusCode();
        final String responseMessage = response.getStatusLine().getReasonPhrase();
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IOException ignored) {
        }
        return create(responseCode, responseMessage, url, is);
    }

    public static HttpResponseException create(int statusCode, String reasonPhrase, String url, InputStream stream) {
        String responseString = "";
        if (stream != null) {
            try {
                responseString = NetworkUtils.readInputStream(stream);
            } catch (IOException ignored) {
            }
        }
        return new HttpResponseException(statusCode, reasonPhrase, url, responseString);
    }


    public HttpResponseException(int httpReturnCode, String message, String url, String response) {
		super(message);
        this.url = url;
        this.httpReturnCode = httpReturnCode;
        this.response = response;
	}

    public HttpResponseException(int httpReturnCode, String message, String url) {
        this(httpReturnCode, message, url, "");
    }

    @Override
    public String getMessage() {
        return String.format("%d %s (%s)", httpReturnCode, super.getMessage() + ". " + response, url);
    }

    public String getResponse() {
        return response;
    }


}
