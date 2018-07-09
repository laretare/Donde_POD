package com.example.danie.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.danie.util.models.Destination;
import com.example.danie.util.models.Origin;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.danie.util.ToolBarSetup.API;


/**
 * Created by danie on 1/27/2018.
 */

public abstract class MainActivityUtil extends AppCompatActivity {
//    private static final int RC_BARCODE_CAPTURE = 9001;
    protected static final String TAG = "BarcodeMain";
//    static final int REQUEST_IMAGE_CAPTURE = 1;
    protected Button scanBtn, pictureBtn;
    protected TextView userName, textComment;
    protected static Origin origin;
    protected static Destination destination;
    protected ImageView mImageView;

    protected abstract Class<?> getMapLookupClass();

    final Handler responseHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            updateScreen();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void updateScreen() {
        userName.setText("Working");
        textComment.setText("Getting info from Server");
    }

    protected void afterResponse(String bol) {
        Log.v(TAG, "Step 4");
        Intent intent = new Intent(this, getMapLookupClass());
        intent.putExtra("Origin", origin);
        intent.putExtra("Destination", destination);
        intent.putExtra("bol_number", bol);
        startActivity(intent);
    }

    public static Origin getOrigin() {
        return origin;
    }

    public static Destination getDestination() {
        return destination;
    }

    protected class InternalRunnable implements Runnable {

        String barcode;

        public InternalRunnable(String barcode) {
            this.barcode = barcode;
        }

        @Override
        public void run() {
            URL test;
            try {
                Log.v(TAG, "Step 1");
                HttpURLConnection connection = makeRequest();
                Log.v(TAG, "Step 2");
                changeScreen();
                processResponse(connection);
                Log.v(TAG, "Step 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @NonNull
        private HttpURLConnection makeRequest() throws IOException {
            URL url;
            url = new URL(API + "/api/v1/dondepod/bol/data");
            HttpURLConnection connection = buildConnection(url, barcode);
            connection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            return connection;
        }

        private void processResponse(HttpURLConnection connection) throws IOException {
            Gson gson = new Gson();
            if (connection.getResponseCode() == 200) {
                readData(connection, gson);
                connection.disconnect();
                afterResponse(barcode);
            } else {
                Log.v(TAG, ""+ connection.getResponseCode());
                Log.v(TAG, "" + connection.getResponseMessage() );
                Log.v(TAG, ""+ connection.getContent().toString());
            }
        }

        private void changeScreen() {
            responseHandler.sendMessage(new Message());
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

                } else if(name.equals("shipment_id")) {
                    ToolBarSetup.SHIPMENT_ID = jsonReader.nextString();
                }
                else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            jsonReader.close();
        }



        @NonNull
        private HttpURLConnection buildConnection(URL test, String bol_number) throws IOException {
            /*String userCredentials = getString(R.string.login);
            byte[] encodeValue = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);
            String encodedAuth = "Basic " + userCredentials;*/

            HttpURLConnection connection = (HttpURLConnection) test.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("bol_number", bol_number);
            connection.setRequestProperty("Content-Type", "application/json");
            return connection;
        }
    }
}
