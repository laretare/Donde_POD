package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.danie.techedgebarcode.models.Destination;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

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
    private Button loginBtn;
    private EditText userNameTxt, password;
    private PlaceHolder placeHolder;
    private Destination destination;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        userNameTxt = (EditText)findViewById(R.id.userName);
        password = (EditText)findViewById(R.id.password);
        loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                // All your networking logic
                                // should be here
                                URL test;
                                try {
                                    test = new URL("http://services.groupkt.com/country/get/iso2code/IN");
                                    HttpURLConnection connection = (HttpURLConnection) test.openConnection();
                                    connection.setRequestProperty("api_key", "4rNh7rsdwijeDXGFYLbEgQAJFjKWWMEE");
                                    connection.setRequestProperty("bol_number", "718041110320001");
                                    connection.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
                                    HttpURLConnection.setFollowRedirects(true);
                                    Gson gson = new Gson();

                                    if (connection.getResponseCode() == 200) {
                                        // Success
                                        // Further processing here
                                        InputStream responseBody = connection.getInputStream();
                                        InputStreamReader responseBodyReader =
                                                new InputStreamReader(responseBody, "UTF-8");
                                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                                        jsonReader.beginObject();
                                        jsonReader.skipValue();
                                        placeHolder = gson.fromJson(jsonReader, PlaceHolder.class);
                                        jsonReader.endObject();
                                        jsonReader.close();
                                        connection.disconnect();
                                    } else {
                                        System.out.println(connection.getResponseCode());

                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        });
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", userNameTxt.getText().toString());
                        startActivity(intent);

                    }


        });

    }





}
