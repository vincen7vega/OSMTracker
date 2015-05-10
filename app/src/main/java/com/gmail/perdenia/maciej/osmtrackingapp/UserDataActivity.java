package com.gmail.perdenia.maciej.osmtrackingapp;

import android.app.Activity;
import android.os.Bundle;

public class UserDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new UserDataFragment())
                .commit();
    }
}
