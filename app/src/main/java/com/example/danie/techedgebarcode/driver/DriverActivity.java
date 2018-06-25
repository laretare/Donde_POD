package com.example.danie.techedgebarcode.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.danie.techedgebarcode.R;

/**
 * Created by danie on 4/16/2018.
 */

public class DriverActivity extends AppCompatActivity {
    private EditText firstName, lastName, phoneNumber;
    private Button sendDriver;
    private Driver driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver);
        sendDriver = (Button) findViewById(R.id.sendDriver);
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        sendDriver.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                driver = new Driver();
                driver.setPhonenumber(phoneNumber.getText().toString());
                sendText();
                Intent intent = new Intent(DriverActivity.this, DriverSentActivity.class);
                intent.putExtra("Driver", driver);
                startActivity(intent);
            }
        });
    }

    private void sendText() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager.getDefault().sendTextMessage(phoneNumber.getText().toString(), null, firstName.getText().toString() + " " + lastName.getText().toString() + " You have been requested to deliver this package", null, null);

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
                    SmsManager.getDefault().sendTextMessage(phoneNumber.getText().toString(), null, firstName.getText().toString() + " " + lastName.getText().toString() + "You have been requested to deliver this package", null, null);


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