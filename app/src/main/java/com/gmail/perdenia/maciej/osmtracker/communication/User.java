package com.gmail.perdenia.maciej.osmtracker.communication;

import com.gmail.perdenia.maciej.osmtracker.gpx.WayPoint;

import java.util.Date;

public class User {

    private int mId;
    private String mName;
    private String mSurname;
    private WayPoint mWayPoint;
    private boolean mActive;

    public User(int id, String name, String surname) {
        mId = id;
        mName = name;
        mSurname = surname;
        mActive = true;
    }

    public User(int id, String name, String surname, WayPoint wayPoint) {
        this(id, name, surname);
        mWayPoint = wayPoint;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setSurname(String surname) {
        mSurname = surname;
    }

    public String getSurname() {
        return mSurname;
    }

    public void setWayPoint(WayPoint wayPoint) {
        mWayPoint = wayPoint;
    }

    public WayPoint getWayPoint() {
        return mWayPoint;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public boolean checkActivity() {
        setActive(true);
        Date currentTime = new Date();
        if (currentTime.getTime() - mWayPoint.getTime().getTime() > 15000) {
            setActive(false);
        }
        return mActive;
    }
}
