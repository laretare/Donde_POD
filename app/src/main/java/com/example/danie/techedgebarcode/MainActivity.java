package com.example.danie.techedgebarcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.danie.techedgebarcode.signature.CaptureSignature;
import com.example.danie.util.MainActivityUtil;
import com.example.danie.util.models.Destination;
import com.example.danie.util.models.Origin;
import com.example.danie.util.ToolBarSetup;


/**
 * Created by Daniel Menard on 1/27/2018.
 */

public class MainActivity extends MainActivityUtil {
  //  private static final int RC_BARCODE_CAPTURE = 9001;
    private Button mSign;
    static final int REQUEST_IMAGE_CAPTURE = 1;


//    final Handler responseHandler = new Handler(Looper.getMainLooper()){
//        @Override
//        public void handleMessage(Message msg) {
//            updateScreen();
//        }
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        userName = (TextView) findViewById(R.id.userName);
        mImageView = (ImageView) findViewById(R.id.imageView);
        textComment = (TextView) findViewById(R.id.textComment);
        mSign = (Button) findViewById(R.id.signature);
        Uri uriData = getIntent().getData();
        if( uriData != null && uriIsValid(uriData)) {
           String bol =  uriData.getQueryParameter("bol");
           InternalRunnable ir = new InternalRunnable(bol);
           AsyncTask.execute(ir);
        } else {
            userName.setText(R.string.Error_bad_bol);
        }
        mSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CaptureSignature.class);
                startActivity(intent);
            }
        });
    }

    protected Class<?> getMapLookupClass(){
        return MapLookup.class;
    }

    public static boolean uriIsValid(Uri uriData){
        String authority = uriData.getAuthority();
        String schemeSpecificPart = uriData.getSchemeSpecificPart();
        String scheme = uriData.getScheme();

        String hashableUri = scheme + ':' + schemeSpecificPart;

        String expectedHash = uriData.getFragment();
        String actualHash = ToolBarSetup.hashMD5(hashableUri);

        return !actualHash.isEmpty() && actualHash.equals(expectedHash);
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

//    private void afterResponse(String bol) {
//        Log.v(TAG, "Step 4");
//        Intent intent = new Intent(this, MapLookup.class);
//        intent.putExtra("Origin", origin);
//        intent.putExtra("Destination", destination);
//        intent.putExtra("bol_number", bol);
//        startActivity(intent);
//    }


    protected void updateScreen() {
        userName.setText("Working");
        textComment.setText("Getting info from Server");

    }

//
//    class InternalRunnable implements Runnable {
//
//        String barcode;
//
//        InternalRunnable(String barcode) {
//            this.barcode = barcode;
//        }
//
//        @Override
//        public void run() {
//            URL test;
//            try {
//                Log.v(TAG, "Step 1");
//                HttpURLConnection connection = makeRequest();
//                Log.v(TAG, "Step 2");
//                changeScreen();
//                processResponse(connection);
//                Log.v(TAG, "Step 3");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @NonNull
//        private HttpURLConnection makeRequest() throws IOException {
//            URL url;
//            url = new URL("http://developmenttest.clearviewaudit.com/api/v1/dondepod/bol/data");
//            HttpURLConnection connection = buildConnection(url, barcode);
//            connection.setInstanceFollowRedirects(true);
//            HttpURLConnection.setFollowRedirects(true);
//            return connection;
//        }
//
//        private void processResponse(HttpURLConnection connection) throws IOException {
//            Gson gson = new Gson();
//            if (connection.getResponseCode() == 200) {
//                readData(connection, gson);
//                connection.disconnect();
//                afterResponse(barcode);
//            } else {
//                Log.v(TAG, ""+ connection.getResponseCode());
//                Log.v(TAG, "" + connection.getResponseMessage() );
//                Log.v(TAG, ""+ connection.getContent().toString());
//            }
//        }
//
//        private void changeScreen() {
//            responseHandler.sendMessage(new Message());
//        }
//
//        private void readData(HttpURLConnection connection, Gson gson) throws IOException {
//            InputStream responseBody = connection.getInputStream();
//
//            InputStreamReader responseBodyReader =
//                    new InputStreamReader(responseBody, "UTF-8");
//            JsonReader jsonReader = new JsonReader(responseBodyReader);
//            jsonReader.beginObject();
//            while (jsonReader.hasNext()) {
//
//                String name = jsonReader.nextName();
//                if (name.equals("origin_stop")) {
//                    origin = gson.fromJson(jsonReader, Origin.class);
//                } else if (name.equals("destination_stop")) {
//                    destination = gson.fromJson(jsonReader, Destination.class);
//
//                } else {
//                    jsonReader.skipValue();
//                }
//            }
//            jsonReader.endObject();
//            jsonReader.close();
//        }
//
//
//
//        @NonNull
//        private HttpURLConnection buildConnection(URL test, String bol_number) throws IOException {
//            /*String userCredentials = getString(R.string.login);
//            byte[] encodeValue = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);
//            String encodedAuth = "Basic " + userCredentials;*/
//
//            HttpURLConnection connection = (HttpURLConnection) test.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("bol_number", bol_number);
//            connection.setRequestProperty("Content-Type", "application/json");
//            return connection;
//        }
//    }
}
