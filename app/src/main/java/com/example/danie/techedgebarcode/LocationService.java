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
import android.location.Address;
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

import com.example.danie.techedgebarcode.models.Destination;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {
    private static String TAG = "LOCATION SERVICE";
    private static final String CHANNEL_ID = "com.example.danie.techedgebarcode.LocationService";
    private static final String CHANNEL_NAME = "DondePOD";
    private LocationManager locationManager = null;
    private static final int locationUpdateTime = 500;
    private static final float location_Distance = 10f;
//    private Context context = this.getApplicationContext();
    LocationListener[] mLocationListeners;
    private Address destinationAddress;
    Destination destination;
    Location destinationLocation;

    public LocationService() {
        Log.v(TAG, "LocationService Constructor");
    }


     @Override
     public IBinder onBind(Intent arg0)
     {
         return null;
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         Log.v(TAG, "LocationService onStartCommand");
         super.onStartCommand(intent, flags, startId);
         destination = (Destination) intent.getSerializableExtra("destination");
         Log.v(TAG, "read destination:" + destination.getAddress() );
         destinationAddress = destination.getAndroidAddressObject(this);
         Log.v(TAG, "latLong: " + destinationAddress.getLatitude() + "," + destinationAddress.getLongitude() );
         destinationLocation = new Location("");
         destinationLocation.setLongitude(destinationAddress.getLongitude());
         destinationLocation.setLatitude(destinationAddress.getLatitude());

         return START_STICKY;
     }

     @Override
     public void onCreate()
     {
         Log.e(TAG, "LocationService onCreate");

         Log.v(TAG, "context=" + this.toString() );
         mLocationListeners = new LocationListener[] {
                 new LocationListener(LocationManager.GPS_PROVIDER, this),
                 new LocationListener(LocationManager.NETWORK_PROVIDER, this)
         };

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

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;
        NotificationManager notificationManager;
        Context context;

        public LocationListener(String provider, Context context)
        {
            Log.e(TAG, "LocationListener " + provider);
            this.context = context;
            createNotificationChannelDependingOnVersion();
            mLastLocation = new Location(provider);
        }

        private void createNotificationChannelDependingOnVersion() {
            Log.v(TAG, "Created NotificationChannel:"+this.toString()+":"+context+":::"+Context.NOTIFICATION_SERVICE);
            notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.v(TAG, "Trying to set notification");
            NotificationCompat.Builder mBuilder;
            if (location.distanceTo(destinationLocation) > 50) {
                mBuilder = new NotificationCompat.Builder(context, "DondePod")
                        .setContentTitle("User location")
                        .setContentText(location.toString())
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
                notificationManager.notify(101, mBuilder.build());
                Log.e(TAG, "onLocationChanged: " + location);
            } else {
                mBuilder = new NotificationCompat.Builder(context, "DondePod")
                        .setContentTitle("arrived")
                        .setContentText("You have arrived at: " + destination.getAddress())
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
                notificationManager.notify(101, mBuilder.build());
                Log.v(TAG, "Got to destination");
                stopSelf();
            }
            Log.v(TAG, "Notification Set");
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
            Log.e(TAG, "onStatusChanged: " + provider + ":" + status + ":"+extras.toString());
        }
    }

}
