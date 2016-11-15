package com.mendeley.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class representing discipline json object.
 *
 */
public class AlternativeName implements Parcelable{

	public String name;

	public AlternativeName() {}

	public AlternativeName(String name) {
		this.name = name;
	}

	public static final Creator<AlternativeName> CREATOR = new Creator<AlternativeName>() {

		@Override
		public AlternativeName createFromParcel(Parcel in) {
			return new AlternativeName(in.readString());
		}

		@Override
		public AlternativeName[] newArray(int size) {
			return new AlternativeName[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(name);
	}
}
