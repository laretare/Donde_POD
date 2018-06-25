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
import com.example.danie.techedgebarcode.signature.CaptureSignature;

public class GeofencePopupActivity extends FragmentActivity {
    private Button scanBtn, rejectBtn, closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.geofence_popup_activity);

//        scanBtn = (Button) findViewById(R.id.getSignatureButton);
//        scanBtn.setOnClickListener(
//                view -> {
//                    Intent intent = new Intent(GeofencePopupActivity.this, CaptureSignature.class);
//                    startActivity(intent);
//                }
//        );
//        rejectBtn= (Button) findViewById(R.id.rejectBtn);
//        rejectBtn.setOnClickListener( view -> {
//            Intent intent = new Intent(GeofencePopupActivity.this, MainActivity.class);
//            startActivity(intent);
//        });
//        closeBtn = (Button) findViewById(R.id.closeBtn);
//        closeBtn.setOnClickListener( view -> {
//           finish();
//        });
        DialogFragment dialog = new GeofenceDialog();
        dialog.show(getFragmentManager(), "arrived");



    }

}
