package com.mendeley.sdk.exceptions;

public class NotSignedInException extends MendeleyException {
	public NotSignedInException() {
		super("User is not signed in");
	}
}
