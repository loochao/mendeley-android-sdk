package com.mendeley.sdk.model;

/**
 * Model class representing discipline json object.
 *
 */
public class Discipline {

	public String name;
	
	public Discipline() {}
	
	public Discipline(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "name: " + name;
	}
}
