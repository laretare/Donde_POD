package com.example.danie.techedgebarcode;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.danie.techedgebarcode.barcode.Scanner;
import com.example.danie.techedgebarcode.models.Destination;
import com.example.danie.techedgebarcode.signature.CaptureSignature;

public class GeofencePopupActivity extends FragmentActivity {
    private Button scanBtn, rejectBtn, closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Destination destination = (Destination) getIntent().getSerializableExtra("destination");
        DialogFragment dialog = new GeofenceDialog();
        Bundle bundle = new Bundle();
        bundle.putString("phoneNumber", destination.getPhone());
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "arrived");



    }

}
