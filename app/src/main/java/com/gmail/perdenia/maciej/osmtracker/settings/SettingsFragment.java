package com.gmail.perdenia.maciej.osmtracker.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
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

    private static final String USER_NAME_KEY = "user-name-key";
    private static final String USER_SURNAME_KEY = "user-surname-key";

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

        EditTextPreference nameEditTextPreference = (EditTextPreference) findPreference(USER_NAME_KEY);
        EditText nameEditText = nameEditTextPreference.getEditText();
        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        nameEditText.setFilters(new InputFilter[]{nameFilter});

        EditTextPreference surnameEditTextPreference =
                (EditTextPreference) findPreference(USER_SURNAME_KEY);
        EditText surnameEditText = surnameEditTextPreference.getEditText();
        InputFilter surnameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && source.charAt(i) != '-' &&
                            source.charAt(i) != ' ') {
                        return "";
                    }
                }
                return null;
            }
        };
        surnameEditText.setFilters(new InputFilter[] { surnameFilter });
    }
}
