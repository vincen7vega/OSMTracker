package com.gmail.perdenia.maciej.osmtrackingapp;

import android.location.Location;

public class User {

    private String mName;
    private String mSurname;
    private Location mLocation;

    public User(String name, String surname) {
        mName = name;
        mSurname = surname;
        mLocation = null;
    }

    public User(String name, String surname, Location location) {
        mName = name;
        mSurname = surname;
        mLocation = location;
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

    public void setLocation(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }
}
