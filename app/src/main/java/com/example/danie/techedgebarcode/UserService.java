package com.example.danie.techedgebarcode;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class UserService extends Service {
    private static String TAG = "LOCATION SERVICE";
    private LocationManager locationManager = null;
    private static final int locationUpdateTime = 500;
    private static final float location_Distance = 10f;

     private class LocationListener implements android.location.LocationListener
     {
         Location mLastLocation;

         public LocationListener(String provider)
         {
             Log.e(TAG, "LocationListener " + provider);
             mLastLocation = new Location(provider);
         }

         @Override
         public void onLocationChanged(Location location)
         {
             Intent notifcationIntent = new Intent(getApplicationContext(), UserService.class);
             notifcationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifcationIntent, 0);
             NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "DondePod")
                     .setContentTitle("User location")
                     .setContentText(location.toString())
                     .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                     .setPriority(NotificationCompat.PRIORITY_DEFAULT);
             NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
             notificationManager.notify(1, mBuilder.build());
             Log.e(TAG, "onLocationChanged: " + location);
             mLastLocation.set(location);
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
         return START_STICKY;
     }

     @Override
     public void onCreate()
     {
         Log.e(TAG, "onCreate");
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
             for (int i = 0; i < mLocationListeners.length; i++) {
                 try {
                     locationManager.removeUpdates(mLocationListeners[i]);
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
