package com.mendeley.sdk.exceptions;

/**
 * Exception that is thrown when a response from the server could not be parsed to a model object.
 * This generally indicates a mismatch between the version of the API understood by the server
 * and by the SDK.
 */
public class JsonParsingException extends MendeleyException {

    public JsonParsingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }


}
