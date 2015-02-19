package com.gmail.perdenia.maciej.osmtrackingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class GpxUploadDialogFragment extends DialogFragment implements GpxUploader.AsyncResponse {

    private final static String GPX_FILENAME_KEY = "gpx-filename-key";
    private final static String USERNAME_KEY = "username-key";
    private final static String PASSWORD_KEY = "password-key";

    Activity mActivity;
    String mGpxFilename;

    public GpxUploadDialogFragment() {

    }

    static GpxUploadDialogFragment newInstance(String gpxFilename) {
        GpxUploadDialogFragment f = new GpxUploadDialogFragment();

        Bundle args = new Bundle();
        args.putString(GPX_FILENAME_KEY, gpxFilename);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGpxFilename = getArguments().getString(GPX_FILENAME_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_text_gpx_upload)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPreferences = PreferenceManager.
                                getDefaultSharedPreferences(getActivity());
                        String username = sharedPreferences.getString(USERNAME_KEY, "");
                        String password = sharedPreferences.getString(PASSWORD_KEY, "");

                        GpxUploader gpxUploader = new GpxUploader(getActivity()
                                .getApplicationContext());
                        gpxUploader.setDelegate(GpxUploadDialogFragment.this);
                        gpxUploader.doUploadGpxTask(
                                username, password, mGpxFilename, "testing", "");
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void processFinish(GpxUploader.Wrapper output) {
        switch (output.mResponseCode) {
            case 200:
                Toast.makeText(mActivity, "Przesyłanie zakończono sukcesem", Toast.LENGTH_LONG).show();
                break;
            case 401:
                Toast.makeText(mActivity, "Niepoprawne dane użytkownika OSM", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(mActivity, output.mResponseCode + " " + output.mResponseMessage,
                        Toast.LENGTH_LONG).show();
        }
    }
}
