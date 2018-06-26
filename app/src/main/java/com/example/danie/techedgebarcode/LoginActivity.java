package com.example.danie.techedgebarcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.danie.techedgebarcode.models.Destination;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by danie on 3/29/2018.
 */

public class LoginActivity extends AppCompatActivity {
    private FileOutputStream outputStream;
    private Button loginBtn;
    private EditText userNameTxt, password;

    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String filename = "user_Profile.txt";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        userNameTxt = (EditText)findViewById(R.id.userName);
        password = (EditText)findViewById(R.id.password);
        loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setBackgroundColor(Color.CYAN);
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                        File file = new File(filename);
                        if (file.exists()){
                            boolean delete = file.delete();
                        }
                       try{
                           outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                           outputStream.write(userNameTxt.getText().toString().getBytes());
                           outputStream.close();
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", userNameTxt.getText().toString());
                        startActivity(intent);

                    }


        });

    }

    private void startUserService() {
        Log.v(TAG, "Starting LocationService");
        Intent userServiceIntent = new Intent(this, LocationUpdate.class);
        startService(userServiceIntent);
        Log.v(TAG, "Finishing Start of LocationService");
    }


}
