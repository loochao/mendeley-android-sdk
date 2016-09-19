package com.mendeley.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class representing discipline json object.
 *
 */
public class Discipline implements Parcelable {

	public String name;
	
	public Discipline() {}
	
	public Discipline(String name) {
		this.name = name;
	}

	public static final Creator<Discipline> CREATOR = new Creator<Discipline>() {

		@Override
		public Discipline createFromParcel(Parcel in) {
			return new Discipline(in.readString());
		}

		@Override
		public Discipline[] newArray(int size) {
			return new Discipline[size];
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

	@Override
	public String toString() {
		return "name: " + name;
	}
}
