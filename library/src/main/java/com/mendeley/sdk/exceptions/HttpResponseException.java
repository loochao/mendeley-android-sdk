package com.mendeley.sdk.exceptions;

/**
 * General exception type that is thrown whenever server returns an error code.
 * <p>
 * This is typically due to invalid parameters (httpReturnCode 4xx),
 * but could also be an internal error (httpReturnCode 5xx).
 */
public class HttpResponseException extends MendeleyException {

    public final String url;
    public final int httpReturnCode;
    public final String httpMessage;
    public final String httpResponse;


    public HttpResponseException(int httpReturnCode, String message, String url) {
        this(httpReturnCode, message, url, "", null);
    }

    public HttpResponseException(int httpReturnCode, String httpMessage, String url, String httpResponse, String mendeleyTraceId) {
        super(httpReturnCode + ": " + httpMessage + " ("+ url + ")" + " " + httpResponse + " X-Mendeley-Trace-Id: " + mendeleyTraceId);
        this.url = url;
        this.httpReturnCode = httpReturnCode;
        this.httpMessage = httpMessage;
        this.httpResponse = httpResponse;
    }

}
