package com.example.danie.techedgebarcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.danie.techedgebarcode.signature.CaptureSignature;
import com.example.danie.util.MainActivityUtil;
import com.example.danie.util.models.Destination;
import com.example.danie.util.models.Origin;
import com.example.danie.util.ToolBarSetup;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static com.example.danie.util.ToolBarSetup.API;


/**
 * Created by Daniel Menard on 1/27/2018.
 */

public class MainActivity extends MainActivityUtil {
    static final int REQUEST_TAKE_PHOTO= 1;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        userName =  findViewById(R.id.userName);
        mImageView =  findViewById(R.id.imageView);
        textComment = findViewById(R.id.textComment);
        Uri uriData = getIntent().getData();
        if( uriData != null && uriIsValid(uriData)) {
           String bol =  uriData.getQueryParameter("bol");
           ToolBarSetup.BOL_NUMBER = bol;
           InternalRunnable ir = new InternalRunnable(bol);
           AsyncTask.execute(ir);
        } else {
            userName.setText(R.string.Error_bad_bol);
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateFormat.getDateInstance().format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private Bitmap setPic() {
        // Get the dimensions of the View
        int targetW = 300;
        int targetH = 300;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    protected Class<?> getMapLookupClass(){
        return MapLookup.class;
    }

    public static boolean uriIsValid(Uri uriData){
        String schemeSpecificPart = uriData.getSchemeSpecificPart();
        String scheme = uriData.getScheme();

        String hashableUri = scheme + ':' + schemeSpecificPart;

        String expectedHash = uriData.getFragment();
        String actualHash = ToolBarSetup.hashMD5(hashableUri);

        return !actualHash.isEmpty() && actualHash.equals(expectedHash);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {


        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {


            AsyncTask.execute(() -> {
                URL url;
                try {
                    url = new URL(API + "/api/v1/dondepod/upload_image");
                    Bitmap imageBitmap = setPic();
                    HttpURLConnection connection = getHttpURLConnection(url);
                    sendImage(imageBitmap, connection);
                    Log.v("log_tag","url: " + url);
                    Scanner result = new Scanner(connection.getInputStream());
                    String response = result.nextLine();
                    Log.e("ImageUploader", "Error uploading image: " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendImage(Bitmap imageBitmap, HttpURLConnection connection) throws IOException {
        OutputStream output = connection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        String image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        image = image.replace(System.getProperty("line.separator"), "");
        String imageJson = "{" +
                "\"shipment_id\" : \"" + ToolBarSetup.SHIPMENT_ID +"\"," +
                "\"image_of\" : \"picture of whatever\"," +
                "\"image\" : \"" + image + "\"" +
                "}";
        osw.write(imageJson);
        osw.flush();
        osw.close();
        output.close();
        connection.connect();
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }


    protected void updateScreen() {
        userName.setText(R.string.work);
        textComment.setText(R.string.info);

    }


}
