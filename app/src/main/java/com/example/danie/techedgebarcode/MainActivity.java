package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.Console;
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

public class MainActivity extends AppCompatActivity {
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    private Button scanBtn;
    private TextView formatTxt, contentTxt, userName;
    private Origin origin;
    private Destination destination;
    private PlaceHolder placeHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        userName = (TextView) findViewById(R.id.userName);
        userName.setText("Welcome \n " + getIntent().getStringExtra("name"));
        scanBtn = (Button) findViewById(R.id.scanBtn);
        placeHolder = new PlaceHolder();
        scanBtn.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, Scanner.class);
                    Log.v(TAG, "Sending intent");
                    startActivityForResult(intent, RC_BARCODE_CAPTURE);
                }
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Toast toast = null;
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                final Barcode barcode = data.getParcelableExtra(Scanner.BarcodeObject);
                InternalRunnable ir = new InternalRunnable(barcode);
                AsyncTask.execute(ir);
            } else {
                doErrorMessage(resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void doErrorMessage(int resultCode) {
        int message_code = 0;
        if (resultCode != CommonStatusCodes.SUCCESS) {
            message_code = R.string.barcode_error;
        } else {
            message_code = R.string.barcode_failure;
        }
        createPopup(message_code);
    }

    private void createPopup(int message_code) {
        Toast toast;
        toast = Toast.makeText(getApplicationContext(), message_code, Toast.LENGTH_LONG);
        toast.show();
    }

    private void createPopup(String message) {
        Toast toast;
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    private void afterResponse() {
        Log.v(TAG, "Step 4");
        Intent intent = new Intent(this, MapLookup.class);
        intent.putExtra("Origin", origin);
        intent.putExtra("Destination", destination);
        startActivity(intent);
    }

    class InternalRunnable implements Runnable {

        Barcode barcode;

        public InternalRunnable(Barcode barcode) {
            this.barcode = barcode;
        }

        @Override
        public void run() {
            URL test;
            try {
                Log.v(TAG, "Step 1");
                HttpURLConnection connection = makeRequest();
                Log.v(TAG, "Step 2");
                processResponse(connection);
                Log.v(TAG, "Step 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @NonNull
        private HttpURLConnection makeRequest() throws IOException {
            URL url;
            url = new URL("http://developmenttest.clearviewaudit.com/api/v1/dondepod/bol/data");
            HttpURLConnection connection = buildConnection(url);
            outputToConnection(connection);
            connection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            return connection;
        }

        private void processResponse(HttpURLConnection connection) throws IOException {
            Gson gson = new Gson();
            if (connection.getResponseCode() == 200) {
//                createPopup("Got Response");
                readData(connection, gson);
                connection.disconnect();
//                createPopup("Loading Map Screen");
                afterResponse();
            } else {
                Log.v(TAG, ""+ connection.getResponseCode());
                Log.v(TAG, "" + connection.getResponseMessage() );
                Log.v(TAG, ""+ connection.getContent().toString());
            }
        }

        private void readData(HttpURLConnection connection, Gson gson) throws IOException {
            InputStream responseBody = connection.getInputStream();

            InputStreamReader responseBodyReader =
                    new InputStreamReader(responseBody, "UTF-8");
            JsonReader jsonReader = new JsonReader(responseBodyReader);
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {

                String name = jsonReader.nextName();
                if (name.equals("origin_stop")) {
                    origin = gson.fromJson(jsonReader, Origin.class);
                } else if (name.equals("destination_stop")) {
                    destination = gson.fromJson(jsonReader, Destination.class);
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            jsonReader.close();
        }

        private void outputToConnection(HttpURLConnection connection) throws IOException {
            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write("{" + "\"api_key\"" + ":" + getString(R.string.api_key) +
                    "\"bol_number\"" + ":" + "\"" + barcode.displayValue + "\"" + "}");
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream
        }

        @NonNull
        private HttpURLConnection buildConnection(URL test) throws IOException {
            String userCredentials = getString(R.string.login);
            byte[] encodeValue = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);
            String encodedAuth = "Basic " + userCredentials;

            HttpURLConnection connection = (HttpURLConnection) test.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", encodedAuth);
            Log.v(TAG, "Encoded:" + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            return connection;
        }
    }
}
