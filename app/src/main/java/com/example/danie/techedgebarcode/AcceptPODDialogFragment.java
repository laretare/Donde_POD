package com.example.danie.techedgebarcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.danie.techedgebarcode.signature.CaptureSignature;

public class AcceptPODDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder popup = new AlertDialog.Builder(getActivity());
        popup.setTitle("Accept?")
                .setPositiveButton("Accept", (DialogInterface dialog, int which) -> {
                    Intent intent = null;
                    Bundle bundle = getArguments();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        intent = new Intent(getContext(), CaptureSignature.class);
                        intent.putExtra("pickup", bundle.getBoolean("pickup"));
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    dismiss();
                });
        return popup.create();
    }

}
