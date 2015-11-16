package com.mendeley.api.exceptions;

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
