package com.gmail.perdenia.maciej.osmtrackingapp;

public class User {

    private String mName;
    private String mSurname;
    private WayPoint mWayPoint;

    public User(String name, String surname) {
        mName = name;
        mSurname = surname;
        mWayPoint = null;
    }

    public User(String name, String surname, WayPoint wayPoint) {
        mName = name;
        mSurname = surname;
        mWayPoint = wayPoint;
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
}
