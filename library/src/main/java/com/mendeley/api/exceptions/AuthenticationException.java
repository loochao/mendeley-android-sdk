package com.mendeley.api.exceptions;

/**
 * Exception that is thrown when authentication has failed.
 */
public class AuthenticationException extends MendeleyException {
	public AuthenticationException(String message, Throwable e) {
		super(message, e);
	}

	public AuthenticationException(String message) {
		this(message, null);
	}
}
