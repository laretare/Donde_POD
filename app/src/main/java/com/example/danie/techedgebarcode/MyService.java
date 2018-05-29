package com.example.danie.techedgebarcode;

import android.app.IntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.techedgebarcode.signature.CaptureSignature;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class MyService extends IntentService {
    private static String TAG = "LOCATION SERVICE";
    private LocationManager locationManager = null;
    private static final int locationUpdateTime = 500;
    private static final float location_Distance = 10f;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public MyService() {

        super("DondePod Location ");
        Log.e(TAG, "GeoFence Service Constructor");
        android.os.Debug.waitForDebugger();

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e(TAG, "onHandle");
        stopService(new Intent(this, LocationService.class));
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Intent notifcationIntent = new Intent(this, MapLookup.class);
        notifcationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifcationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "error in geofence");
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e(TAG, "notification");
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geo : triggeringGeofences) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "DondePod")
                        .setContentTitle("User location")
                        .setContentText(geo.getRequestId())
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify( 1, mBuilder.build());
            }
                Intent nextScreen = new Intent(this, CaptureSignature.class);
                nextScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


        } else {
            Log.e(TAG, "error in geofence");
            startService(new Intent(this, LocationService.class));
        }
    }

}


