package com.mendeley.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Box implements Parcelable {
    public final Point topLeft;
    public final Point bottomRight;
    public final Integer page;

    public Box(Point topLeft, Point bottomRight, Integer page) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.page = page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (bottomRight != null ? !bottomRight.equals(box.bottomRight) : box.bottomRight != null)
            return false;
        if (page != null ? !page.equals(box.page) : box.page != null) return false;
        if (topLeft != null ? !topLeft.equals(box.topLeft) : box.topLeft != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topLeft != null ? topLeft.hashCode() : 0;
        result = 31 * result + (bottomRight != null ? bottomRight.hashCode() : 0);
        result = 31 * result + (page != null ? page.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(topLeft.x);
        dest.writeDouble(topLeft.y);
        dest.writeDouble(bottomRight.x);
        dest.writeDouble(bottomRight.y);
        dest.writeInt(page);
    }

    public static final Creator<Box> CREATOR = new Creator<Box>() {
        public Box createFromParcel(Parcel in) {
            return new Box(new Point(in.readDouble(), in.readDouble()), new Point(in.readDouble(), in.readDouble()), in.readInt());
        }

        public Box[] newArray(int size) {
            return new Box[size];
        }
    };

}
