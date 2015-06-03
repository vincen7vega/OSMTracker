package com.gmail.perdenia.maciej.osmtracker.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.perdenia.maciej.osmtracker.R;

public class UploadDialogFragment extends DialogFragment {

    public interface SendCancelListener {
        void uploadOnSend(EditText name, EditText description);
        void uploadOnCancel();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SendCancelListener)) {
            throw new ClassCastException(
                    activity.toString() +
                            " musi implementować UploadDialogFragment.SendCancelListener");
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

        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int inner_top_margin = (int) getResources().getDimension(R.dimen.dialog_margin_inner_top);
        headingParams.setMargins(margin, inner_top_margin, margin, 0);

        TextView nameHeading = new TextView(getActivity());
        nameHeading.setText(getResources().getString(R.string.dialog_text_upload_name));
        nameHeading.setTextColor(getResources().getColor(R.color.primary_text));
        layout.addView(nameHeading, headingParams);

        final EditText name = new EditText(getActivity());
        layout.addView(name, params);

        TextView descHeading = new TextView(getActivity());
        descHeading.setText(getResources().getString(R.string.dialog_text_upload_description));
        descHeading.setTextColor(getResources().getColor(R.color.primary_text));
        layout.addView(descHeading, headingParams);

        final EditText description = new EditText(getActivity());
        layout.addView(description, params);

        dialog.setView(layout);

        dialog.setPositiveButton(getResources().getString(R.string.dialog_btn_send),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((SendCancelListener) getActivity()).uploadOnSend(name, description);
                    }
                });
        dialog.setNegativeButton(getString(R.string.dialog_btn_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        ((SendCancelListener) getActivity()).uploadOnCancel();
                    }
                });

        return dialog.create();
    }
}
