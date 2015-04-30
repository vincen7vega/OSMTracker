package com.gmail.perdenia.maciej.osmtrackingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class WayPoint implements Parcelable {

    private double mLatitude;
    private double mLongitude;
    private String mTimeStamp;

    public WayPoint(double latitude, double longitude, String timeStamp) {
        mLatitude = latitude;
        mLongitude = longitude;
        mTimeStamp = timeStamp;
    }

    public WayPoint(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mTimeStamp = in.readString();
    }

    public double getLat() {
        return mLatitude;
    }

    public double getLon() {
        return mLongitude;
    }

    public String getTime() {
        return mTimeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mTimeStamp);
    }

    public static final Creator CREATOR =
            new Creator() {
                public WayPoint createFromParcel(Parcel in) {
                    return new WayPoint(in);
                }

                public WayPoint[] newArray(int size) {
                    return new WayPoint[size];
                }
            };
}
