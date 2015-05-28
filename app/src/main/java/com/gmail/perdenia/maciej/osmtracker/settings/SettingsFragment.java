package com.gmail.perdenia.maciej.osmtracker.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.gmail.perdenia.maciej.osmtracker.R;

import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragment {

    private final static String SERVER_IP_KEY = "server-ip-key";
    private final static String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private Pattern mPattern;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPattern = Pattern.compile(IP_ADDRESS_PATTERN);

        addPreferencesFromResource(R.xml.settings);
        findPreference(SERVER_IP_KEY).setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean valid = mPattern.matcher(newValue.toString()).matches();
                        if (valid) {
                            return true;
                        } else {
                            Toast.makeText(getActivity(), "Niepoprawny adres IP", Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }
                    }
                });
    }
}
