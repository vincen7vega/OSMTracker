package com.gmail.perdenia.maciej.osmtrackingapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WayPoint implements Parcelable {

    public static final String TAG = WayPoint.class.getSimpleName();

    private double mLatitude;
    private double mLongitude;
    private double mElevation;
    private Date mTime;

    public WayPoint(double latitude, double longitude, Date time) {
        mLatitude = latitude;
        mLongitude = longitude;
        mElevation = 0.0;
        mTime = time;
    }

    public WayPoint(double latitude, double longitude, String time) {
        mLatitude = latitude;
        mLongitude = longitude;
        mElevation = 0.0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            mTime = sdf.parse(time);
        } catch (ParseException e) {
            Log.w(TAG, "Złapano wyjątek przy parsowaniu time stamp'a (ParseException)");
            e.printStackTrace();
        }
    }

    public WayPoint(double latitude, double longitude, Date time, double elevation) {
        this(latitude, longitude, time);
        mElevation = elevation;
    }

    public WayPoint(double latitude, double longitude, String time, double elevation) {
        this(latitude, longitude, time);
        mElevation = elevation;
    }

    public WayPoint(Parcel in) {
        readFromParcel(in);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setElevation(double elevation) {
        mElevation = elevation;
    }

    public double getElevation() {
        return mElevation;
    }

    public Date getTime() {
        return mTime;
    }

    public String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(mTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mElevation = in.readDouble();
        mTime = (Date) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeDouble(mElevation);
        dest.writeSerializable(mTime);
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
