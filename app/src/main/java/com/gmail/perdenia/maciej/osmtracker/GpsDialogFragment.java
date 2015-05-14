package com.gmail.perdenia.maciej.osmtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class GpsDialogFragment extends DialogFragment {

    public interface OkCancelListener {
        void gpsOnOk();
        void gpsOnCancel();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OkCancelListener)) {
            throw new ClassCastException(
                    activity.toString() + " musi implementować GpsDialogFragment.OkCancelListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getResources().getString(R.string.dialog_title_enable_gps));
        dialog.setMessage(getResources().getString(R.string.dialog_text_enable_gps));
        dialog.setIcon(R.drawable.dialog_icon_gps);
        dialog.setPositiveButton(getResources().getString(R.string.dialog_btn_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((OkCancelListener) getActivity()).gpsOnOk();
                    }
                });
        dialog.setNegativeButton(getString(R.string.dialog_btn_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((OkCancelListener) getActivity()).gpsOnCancel();
                    }
                });

        return dialog.create();
    }
}
