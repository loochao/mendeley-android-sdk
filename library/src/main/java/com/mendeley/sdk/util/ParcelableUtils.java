package com.mendeley.sdk.util;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParcelableUtils {


    public static boolean readBoolFromParcel(Parcel in) {
        return in.readInt() > 0;
    }

    public static void writeBoolToParcel(Parcel in, boolean value) {
        in.writeInt(value ? 1 : 0);
    }

    public static Date readDateFromParcel(Parcel in) {
        return new Date(in.readLong());
    }

    public static void writeDateToParcel(Parcel in, Date value) {
        in.writeLong(value.getTime());
    }


    // Optionals / Nullables


    public static Boolean readOptionalBooleanFromParcel(Parcel parcel) {
        if (readBoolFromParcel(parcel)) {
            return readBoolFromParcel(parcel);
        } else {
            return null;
        }
    }

    public static void writeOptionalBooleanToParcel(Parcel parcel, Boolean value) {
        if (value != null) {
            writeBoolToParcel(parcel, true);
            writeBoolToParcel(parcel, value);
        } else {
            writeBoolToParcel(parcel, false);
        }
    }

    public static Integer readOptionalIntegerFromParcel(Parcel parcel) {
        if (readBoolFromParcel(parcel)) {
            return parcel.readInt();
        } else {
            return null;
        }
    }

    public static void writeOptionalIntegerToParcel(Parcel parcel, Integer value) {
        if (value != null) {
            writeBoolToParcel(parcel, true);
            parcel.writeInt(value);
        } else {
            writeBoolToParcel(parcel, false);
        }
    }

    public static String readOptionalStringFromParcel(Parcel parcel) {
        if (readBoolFromParcel(parcel)) {
            return parcel.readString();
        } else {
            return null;
        }
    }

    public static void writeOptionalStringToParcel(Parcel parcel, String value) {
        if (value != null) {
            writeBoolToParcel(parcel, true);
            parcel.writeString(value);
        } else {
            writeBoolToParcel(parcel, false);
        }
    }


    public static Date readOptionalDateFromParcel(Parcel parcel) {
        if (readBoolFromParcel(parcel)) {
            return readDateFromParcel(parcel);
        } else {
            return null;
        }
    }


    public static void writeOptionalDateToParcel(Parcel parcel, Date value) {
        if (value != null) {
            writeBoolToParcel(parcel, true);
            writeDateToParcel(parcel, value);
        } else {
            writeBoolToParcel(parcel, false);
        }
    }

    public static  <T extends Parcelable> T readOptionalParcelableFromParcel(Parcel parcel, ClassLoader loader) {
        if (readBoolFromParcel(parcel)) {
            return parcel.readParcelable(loader);
        } else {
            return null;
        }
    }

    public static void writeOptionalParcelableToParcel(Parcel parcel, Parcelable value, int flags) {
        if (value != null) {
            writeBoolToParcel(parcel, true);
            parcel.writeParcelable(value, flags);
        } else {
            writeBoolToParcel(parcel, false);
        }
    }


}
