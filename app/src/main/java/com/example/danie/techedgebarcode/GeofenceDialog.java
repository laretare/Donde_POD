package com.example.danie.techedgebarcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.example.danie.techedgebarcode.signature.CaptureSignature;


public class GeofenceDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder popup = new AlertDialog.Builder(getActivity());
        popup.setMessage("You have arrived.")
                .setPositiveButton("Deliver", (DialogInterface dialog, int which) -> {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        intent = new Intent(getContext(), CaptureSignature.class);
                    }
                    startActivity(intent);
                })
                .setNeutralButton("Call Location", (dialog, which) -> {
                    Bundle bundle = getArguments();
                    String phoneNumber = bundle.getString("phoneNumber");
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);

                })
                .setNegativeButton("X", (dialog, which) -> dismiss());

        return popup.create();
    }
}
