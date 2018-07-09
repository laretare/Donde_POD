package com.example.danie.techedgebarcode;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;

import com.example.danie.util.models.Destination;

public class AcceptPODDialogFragmentActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogFragment dialog = new AcceptPODDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("pickup", (Boolean) getIntent().getSerializableExtra("pickup"));
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "Pickup");

    }

}
