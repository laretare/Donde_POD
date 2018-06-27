package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.danie.techedgebarcode.models.Destination;
import com.example.danie.techedgebarcode.models.Origin;
import com.google.android.gms.common.api.CommonStatusCodes;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by danie on 1/27/2018.
 */

public class MainActivity extends AppCompatActivity {
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button scanBtn, pictureBtn;
    private TextView userName, textComment;
    private Origin origin;
    private Destination destination;
    private ImageView mImageView;
    final Handler responseHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            updateScreen();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ToolBarSetup.setupToolBar(this);
        userName = (TextView) findViewById(R.id.userName);
        userName.setText(String.format(" Welcome, \n%s", getIntent().getStringExtra("name")));
        mImageView = (ImageView) findViewById(R.id.imageView);
        textComment = (TextView) findViewById(R.id.textComment);


        Uri uriData = getIntent().getData();
        String uriDataString = uriData.toString();
        //verify uriDataString
        int lastIndex = uriDataString.lastIndexOf('/');
        String uri = uriDataString.substring(0, lastIndex);
        String expectedHash = uriDataString.substring(lastIndex+1);
        String actualHash = ToolBarSetup.hashMD5(uri);
        if(actualHash.equals(expectedHash)) {
           String bol =  uri.substring(uri.lastIndexOf('/')+1);
            InternalRunnable ir = new InternalRunnable(bol);
            AsyncTask.execute(ir);
        } else {
            userName.setText(R.string.Error_bad_bol);
        }

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

        }
        else {
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

    private void afterResponse(String bol) {
        Log.v(TAG, "Step 4");
        Intent intent = new Intent(this, MapLookup.class);
        intent.putExtra("Origin", origin);
        intent.putExtra("Destination", destination);
        intent.putExtra("bol_number", bol);
        startActivity(intent);
    }


    private void updateScreen() {
        userName.setText("Working");
        textComment.setText("Getting info from Server");

    }


    class InternalRunnable implements Runnable {

        String barcode;

        InternalRunnable(String barcode) {
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
            url = new URL("http://developmenttest.clearviewaudit.com/api/v1/dondepod/bol/data");
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

                } else {
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
