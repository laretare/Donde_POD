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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;

public class MyService extends IntentService {
    private static String TAG = "LOCATION SERVICE";
    private LocationManager locationManager = null;
    private static final int locationUpdateTime = 500;
    private static final float location_Distance = 10f;
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public MyService() {
        super("DondePod Location");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Intent notifcationIntent = new Intent(getApplicationContext(), MyService.class);
        createNotificationChannel();
        notifcationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifcationIntent, 0);
        if (geofencingEvent.hasError()) {

            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geo : triggeringGeofences) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "DondePod")
                        .setContentTitle("User location")
                        .setContentText(geo.getRequestId())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(1, mBuilder.build());
            }


        } else {
            //  get user's current location
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "DondePod")
                            .setContentTitle("User location")
                            .setContentText("Error No permission granted for location")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(1, mBuilder.build());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "DondePod")
                        .setContentTitle("User location")
                        .setContentText("Error No permission granted for location")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(1, mBuilder.build());
                return;
            }
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            }

        }

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = "Notification for app test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("DondePod", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

