package com.example.danie.techedgebarcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.danie.techedgebarcode.signature.CaptureSignature;
import com.example.danie.util.ToolBarSetup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.danie.util.ToolBarSetup.API;

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
                    try {
                        buildRequest();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    dismiss();
                });
        return popup.create();
    }

    private void buildRequest() throws IOException {

            URL url = new URL(API + "/api/v1/dondepod/mark_that_driver_accepted_shipment");
            HttpURLConnection connection = buildConnection(url);
            connection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
    }


    @NonNull
    private HttpURLConnection buildConnection(URL apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("donde_pod_code", ToolBarSetup.DONDEPOD_CODE);
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }



}
