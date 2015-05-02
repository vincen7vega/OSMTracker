package com.gmail.perdenia.maciej.osmtrackingapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WayPoint implements Parcelable {

    private double mLatitude;
    private double mLongitude;
    private double mAltitude;
    private Date mTimeStamp;

    public WayPoint(double latitude, double longitude, Date timeStamp) {
        mLatitude = latitude;
        mLongitude = longitude;
        mTimeStamp = timeStamp;
    }

    public WayPoint(double latitude, double longitude, String timeStamp) {
        mLatitude = latitude;
        mLongitude = longitude;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            mTimeStamp = sdf.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public WayPoint(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mTimeStamp = (Date) in.readSerializable();
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public Date getTime() {
        return mTimeStamp;
    }

    public String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(mTimeStamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeSerializable(mTimeStamp);
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
