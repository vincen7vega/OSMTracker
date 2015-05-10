package com.gmail.perdenia.maciej.osmtrackingapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class UserDataFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.user_data);
    }
}
