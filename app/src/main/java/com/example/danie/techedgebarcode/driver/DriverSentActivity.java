package com.example.danie.techedgebarcode.driver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.danie.techedgebarcode.R;
import com.example.danie.techedgebarcode.driver.Driver;

/**
 * Created by danie on 4/16/2018.
 */

public class DriverSentActivity extends AppCompatActivity {
    private TextView name, phoneNumber;
    private Button  resend;
    private Driver driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sent_driver);
        resend = (Button) findViewById(R.id.resend);
        name = (TextView) findViewById(R.id.driverName);

        phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        driver = (Driver) getIntent().getSerializableExtra("Driver");
        phoneNumber.setText(driver.getPhonenumber());

        resend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendText();
            }
        });
    }

    private void sendText() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager.getDefault().sendTextMessage(phoneNumber.getText().toString(), null,   " You have been requested to deliver this package", null, null);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager.getDefault().sendTextMessage(phoneNumber.getText().toString(), null,  "You have been requested to deliver this package", null, null);


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
