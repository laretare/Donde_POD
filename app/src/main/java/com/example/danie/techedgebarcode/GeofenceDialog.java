package com.example.danie.techedgebarcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.example.danie.techedgebarcode.signature.CaptureSignature;


public class GeofenceDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder popup = new AlertDialog.Builder(getActivity());
        popup.setMessage("You have arrived.")
                .setPositiveButton("Deliver", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // FIRE ZE MISSILES!
                        Intent intent = new Intent(getContext(), CaptureSignature.class);
                        startActivity(intent);
                    }
                })
                .setNeutralButton("Call Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Call Location
                    }
                })
                .setNegativeButton("X", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Close
                        dismiss();
                    }
                });

        return popup.create();
    }
}
