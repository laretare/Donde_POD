package com.example.danie.techedgebarcode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Base64;
import android.util.Log;


import com.example.danie.techedgebarcode.driver.Driver;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class LocationUpdate extends Service {
    private static String TAG = "LOCATION SERVICE";
    private LocationManager locationManager = null;
    private static final long locationUpdateTime = 5000;
    private static final float location_Distance = 10f;
    private  String bol_Number = null;

     private class LocationListener implements android.location.LocationListener
     {
         Location mLastLocation;
         String city;
         String state;
         public LocationListener(String provider)
         {

             Log.e(TAG, "LocationListener " + provider);
             mLastLocation = new Location(provider);

         }

         @Override
         public void onLocationChanged(Location location)
         {
             Geocoder geocoder;
             List<Address> addressList;
             NotificationCompat.Builder mBuilder;
             geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
             try {
                 addressList = geocoder.getFromLocation(25.739215, -100.518236, 1);
                 city = addressList.get(0).getLocality();
                 state = addressList.get(0).getAdminArea();

             } catch (IOException e) {
                 e.printStackTrace();
             }
             Intent notificationIntent = new Intent(getApplicationContext(), MapLookup.class);
             notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
             if ( city == null ){
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), "DondePod")
                         .setContentTitle("Shipment current location")
                         .setContentText(location.getLatitude() + " " + location.getLongitude())
                         .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                         .setContentIntent(pendingIntent)
                         .setAutoCancel(true);
             } else {
                 mBuilder = new NotificationCompat.Builder(getApplicationContext(), "DondePod")
                         .setContentTitle("Shipment current location")
                         .setContentText(city + " " + state)
                         .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                         .setContentIntent(pendingIntent)
                         .setAutoCancel(true);
             }
             NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
             notificationManager.notify(1, mBuilder.build());
             mLastLocation.set(location);
             InternalRunnable ir = new InternalRunnable();
             AsyncTask.execute(ir);
             Log.e(TAG, "onLocationChanged: " + location);

         }
         class InternalRunnable implements Runnable {

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
                 url = new URL("http://developmenttest.clearviewaudit.com/api/v1/dondepod/trackingevent");
                 HttpURLConnection connection = buildConnection(url);
                 outputToConnection(connection);
                 connection.setInstanceFollowRedirects(true);
                 HttpURLConnection.setFollowRedirects(true);
                 return connection;
             }
             private void readData(HttpURLConnection connection, Gson gson) throws IOException {
                 InputStream responseBody = connection.getInputStream();

                 InputStreamReader responseBodyReader =
                         new InputStreamReader(responseBody, "UTF-8");
                 JsonReader jsonReader = new JsonReader(responseBodyReader);
                 jsonReader.beginObject();
                 while (jsonReader.hasNext()) {
                     String name = jsonReader.nextName();

                 }
                 jsonReader.endObject();
                 jsonReader.close();
             }

             private void processResponse(HttpURLConnection connection) throws IOException {
                 Gson gson = new Gson();
                 if (connection.getResponseCode() == 200) {

                     readData(connection, gson);
                     connection.disconnect();


                 } else {
                     Log.d(TAG, "" + connection.getResponseCode());
                     Log.d(TAG, "" + connection.getResponseMessage());
                     Log.d(TAG, "" + connection.getContent().toString());
                 }
             }
             @NonNull
             private HttpURLConnection buildConnection(URL test) throws IOException {
                 String userCredentials = getString(R.string.login);
                 byte[] encodeValue = Base64.encode(userCredentials.getBytes(), Base64.DEFAULT);
                 String encodedAuth = "Basic " + userCredentials;

                 HttpURLConnection connection = (HttpURLConnection) test.openConnection();
                 connection.setRequestMethod("POST");
                 //connection.setRequestProperty("Authorization", encodedAuth);

                 connection.setRequestProperty("Content-Type", "application/json");
                 connection.setDoOutput(true);
                 return connection;
             }
         }
         private void outputToConnection(HttpURLConnection connection) throws IOException {
             Driver driver = new Driver("bob", "jones", "555-555-5555");
             String trackingJson =
                     "{" +
                    "\"api_key\" :" + getString(R.string.api_key) +
                    "\"tracking_info\":{" +
                            "\"shipment_number\": \"" + bol_Number + "\"," +
                            "\"event\":   \"tracking\","  +
                             "\"driver_info\": {" +
                             "\"first\": \"" + driver.getFirstName() + "\"," +
                             "\"last\": \"" + driver.getLastName() + "\"," +
                             "\"phone\": \"" + driver.getPhonenumber() + "\"" +
                                "}," +
                             "\"location\":   {" +
                             "\"latitude\": " + mLastLocation.getLatitude() + "," +
                             "\"longitude\":" + mLastLocation.getLongitude() +
                            "}" +
                         "}" +
                     "}";
             Log.e(TAG, trackingJson);

             OutputStream os = connection.getOutputStream();

             OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

             osw.write(trackingJson);
             osw.flush();
             osw.close();
             os.close();  //don't forget to close the OutputStream
        }

         @Override
         public void onProviderDisabled(String provider)
         {
             Log.e(TAG, "onProviderDisabled: " + provider);
         }

         @Override
         public void onProviderEnabled(String provider)
         {
             Log.e(TAG, "onProviderEnabled: " + provider);
         }

         @Override
         public void onStatusChanged(String provider, int status, Bundle extras)
         {
             Log.e(TAG, "onStatusChanged: " + provider);
         }
     }

     LocationListener[] mLocationListeners = new LocationListener[] {
             new LocationListener(LocationManager.GPS_PROVIDER),
             new LocationListener(LocationManager.NETWORK_PROVIDER)
     };

     @Override
     public IBinder onBind(Intent arg0)
     {
         return null;
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         Log.e(TAG, "onStartCommand");

         super.onStartCommand(intent, flags, startId);
         bol_Number = intent.getStringExtra("bol_number");
         return START_STICKY;
     }

     @Override
     public void onCreate()
     {
         Log.e(TAG, "onCreate");
         android.os.Debug.waitForDebugger();
         initializeLocationManager();

         try {
             locationManager.requestLocationUpdates(
                     LocationManager.NETWORK_PROVIDER, locationUpdateTime , location_Distance ,
                     mLocationListeners[1]);
         } catch (java.lang.SecurityException ex) {
             Log.i(TAG, "fail to request location update, ignore", ex);
         } catch (IllegalArgumentException ex) {
             Log.d(TAG, "network provider does not exist, " + ex.getMessage());
         }
         try {
             locationManager.requestLocationUpdates(
                     LocationManager.GPS_PROVIDER, locationUpdateTime , location_Distance ,
                     mLocationListeners[0]);
         } catch (java.lang.SecurityException ex) {
             Log.i(TAG, "fail to request location update, ignore", ex);
         } catch (IllegalArgumentException ex) {
             Log.d(TAG, "gps provider does not exist " + ex.getMessage());
         }
     }

     @Override
     public void onDestroy()
     {
         Log.e(TAG, "onDestroy");
         super.onDestroy();
         if (locationManager != null) {
             for (LocationListener mLocationListener : mLocationListeners) {
                 try {
                     locationManager.removeUpdates(mLocationListener);
                 } catch (Exception ex) {
                     Log.i(TAG, "fail to remove location listners, ignore", ex);
                 }
             }
         }
     }

     private void initializeLocationManager() {
         Log.e(TAG, "initializeLocationManager");
         if (locationManager == null) {
             locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
         }
     }


}
