package com.gmail.perdenia.maciej.osmtrackingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackPoint implements Parcelable {
    private double mLatitude;
    private double mLongitude;
    private String mTimeStamp;

    public TrackPoint(double latitude, double longitude, String timeStamp) {
        mLatitude = latitude;
        mLongitude = longitude;
        mTimeStamp = timeStamp;
    }

    public TrackPoint(Parcel in) {
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
                public TrackPoint createFromParcel(Parcel in) {
                    return new TrackPoint(in);
                }

                public TrackPoint[] newArray(int size) {
                    return new TrackPoint[size];
                }
            };
}
