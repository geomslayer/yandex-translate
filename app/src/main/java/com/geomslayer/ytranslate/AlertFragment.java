package com.geomslayer.ytranslate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class AlertFragment extends DialogFragment {

    public static final String TITLE = "title";

    public static AlertFragment newInstance(@NonNull String title) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);

        AlertFragment fragment = new AlertFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString(TITLE);
        builder.setTitle(title);
        String mask = getString(R.string.areYouShure);
        builder.setMessage(String.format(mask, title.toLowerCase()));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogListener listener = (DialogListener) getTargetFragment();
                listener.onPositiveButtonClick();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        return builder.create();
    }

    public interface DialogListener {
        void onPositiveButtonClick();
    }
}
