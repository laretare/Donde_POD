package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import com.amazonaws.http.HttpClient;
import com.example.danie.techedgebarcode.barcode.Scanner;
import com.example.danie.techedgebarcode.models.Destination;
import com.example.danie.techedgebarcode.models.Origin;
import com.google.android.gms.common.api.CommonStatusCodes;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.http.client.methods.HttpGetHC4;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;



/**
 * Created by danie on 1/27/2018.
 */

public class MainActivity extends AppCompatActivity  {
    private Button scanBtn;
    private TextView formatTxt, contentTxt, userName;
    private Origin origin;
    private Destination destination;
    private PlaceHolder placeHolder;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        userName = (TextView)findViewById(R.id.userName);
        userName.setText( "Welcome \n " + getIntent().getStringExtra("name"));
        scanBtn = (Button)findViewById(R.id.scanBtn);
        placeHolder = new PlaceHolder();
        scanBtn.setOnClickListener( new View.OnClickListener(){

            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Scanner.class);


                startActivityForResult(intent, RC_BARCODE_CAPTURE);

            }
        });
       /* getSignature = (Button) findViewById(R.id.delivery);
        getSignature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CaptureSignature.class);
                startActivity(intent);
            }
        });
*/



    }
// after scanning is done
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Toast toast = null;
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                // checking if barcode was captured
                if (data != null) {
                    final Barcode barcode = data.getParcelableExtra(Scanner.BarcodeObject);
                       AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                URL test;
                                try {

                                    test = new URL("http://developmenttest.clearviewaudit.com/api/v1/dondepod/bol/data");
                                    HttpURLConnection connection = (HttpURLConnection) test.openConnection();


                                    String userCredentials = "kgriffin@clearviewaudit.com:Javag33K";
                                    byte[] encodeValue = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);

                                    String encodedAuth= "Basic "+ userCredentials;
                                    // setting requests
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Authorization",encodedAuth);
                                    connection.setRequestProperty("Content-Type","application/json");
                                    connection.setDoOutput(true);
                                    OutputStream os =  connection.getOutputStream();
                                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                                    osw.write("{"+"\"api_key\""+":"+"\"TuM6rJL9i2HYrSenELXKykLSM8Dz5SFj\","+
                                            "\"bol_number\""+":"+"\""+ barcode.displayValue + "\"" + "}");
                                    osw.flush();
                                    osw.close();
                                    os.close();  //don't forget to close the OutputStream

                                    // setting up handling for redirects
                                    connection.setInstanceFollowRedirects(true);
                                    HttpURLConnection.setFollowRedirects(true);
                                    Gson gson = new Gson();

                                    if (connection.getResponseCode() == 200) {
                                        // Success
                                        // getting response
                                        InputStream responseBody = connection.getInputStream();
                                        InputStreamReader responseBodyReader =
                                                new InputStreamReader(responseBody, "UTF-8");
                                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                                        jsonReader.beginObject();
                                        while(jsonReader.hasNext()) {
                                            String name = jsonReader.nextName();
                                            if(name.equals("origin_stop")) {
                                                origin = gson.fromJson(jsonReader, Origin.class);
                                            }
                                            else if (name.equals("destination_stop")) {
                                                destination = gson.fromJson(jsonReader, Destination.class);
                                            } else {
                                                jsonReader.skipValue();
                                            }
                                        }
                                        jsonReader.endObject();
                                        jsonReader.close();
                                        connection.disconnect();
                                        afterResponse();
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
                  // barcode was not captured
                } else {
                    toast =  Toast.makeText(getApplicationContext(), R.string.barcode_failure, Toast.LENGTH_LONG);
                    toast.show();

                }
            // unable to scan barcode
            } else {
                toast =  Toast.makeText(getApplicationContext(), R.string.barcode_error, Toast.LENGTH_LONG);
                toast.show();

            }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
    private void afterResponse() {
        Intent intent = new Intent(this, MapLookup.class);
        intent.putExtra("Origin", origin);
        intent.putExtra("Destination", destination);
        startActivity(intent);
    }

}
