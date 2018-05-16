package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.danie.techedgebarcode.signature.CaptureSignature;

public class endScreen extends AppCompatActivity {
    private Button getSignature ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_screen);
        getSignature = (Button) findViewById(R.id.delivery);
        getSignature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(endScreen.this, CaptureSignature.class);
                startActivity(intent);
            }
        });
    }
}
