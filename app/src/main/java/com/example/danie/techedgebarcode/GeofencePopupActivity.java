package com.example.danie.techedgebarcode;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;

import com.example.danie.util.models.Destination;

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
