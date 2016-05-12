package com.mendeley.sdk.model;

/**
 * Model class representing photo json object.
 *
 */
public class Photo {

	public final String original;
	public final String standard;
	public final String square;

	public Photo(String original, String standard, String square) {
		this.original = original;
		this.standard = standard;
		this.square = square;
	}

	@Override
	public String toString() {
		return "Photo{" +
				"original='" + original + '\'' +
				", standard='" + standard + '\'' +
				", square='" + square + '\'' +
				'}';
	}
}
