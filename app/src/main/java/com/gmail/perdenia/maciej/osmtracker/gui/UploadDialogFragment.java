package com.gmail.perdenia.maciej.osmtracker.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.gmail.perdenia.maciej.osmtracker.R;

public class UploadDialogFragment extends DialogFragment {

    public interface OkCancelListener {
        void uploadOnOk(EditText input);
        void uploadOnCancel();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OkCancelListener)) {
            throw new ClassCastException(
                    activity.toString() +
                            " musi implementować UploadDialogFragment.OkCancelListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getResources().getString(R.string.dialog_title_upload_gpx));
        dialog.setMessage(getResources().getString(R.string.dialog_text_upload_gpx));
        dialog.setIcon(R.drawable.dialog_icon_upload);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.setMargins(margin, 0, margin, 0);
        final EditText input = new EditText(getActivity());
        layout.addView(input, params);
        dialog.setView(layout);

        dialog.setPositiveButton(getResources().getString(R.string.dialog_btn_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((OkCancelListener) getActivity()).uploadOnOk(input);
                    }
                });
        dialog.setNegativeButton(getString(R.string.dialog_btn_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((OkCancelListener) getActivity()).uploadOnCancel();
                    }
                });

        return dialog.create();
    }
}
