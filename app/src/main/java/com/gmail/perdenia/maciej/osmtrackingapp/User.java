package com.gmail.perdenia.maciej.osmtrackingapp;

public class User {

    private int mId;
    private String mName;
    private String mSurname;
    private WayPoint mWayPoint;

    public User(int id, String name, String surname) {
        mId = id;
        mName = name;
        mSurname = surname;
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
}
