package com.mendeley.sdk.exceptions;

public class NotSignedInException extends RuntimeException {
	public NotSignedInException() {
		super("User is not signed in");
	}
}
