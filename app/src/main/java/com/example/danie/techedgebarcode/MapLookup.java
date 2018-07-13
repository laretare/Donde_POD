package com.example.danie.techedgebarcode;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


import com.example.danie.techedgebarcode.signature.CaptureSignature;
import com.example.danie.util.models.Destination;
import com.example.danie.util.models.Origin;
import com.example.danie.util.ToolBarSetup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.geometry.LatLng;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.danie.util.ToolBarSetup.API;

/**
 * Created by Daniel Menard on 1/25/2018.
 */

public class MapLookup extends AppCompatActivity implements LocationEngineListener, PermissionsListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "DirectionsActivity";
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private LocationRequest locationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Destination destination;
    private Origin origin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent() != null){
            String data = getIntent().getExtras().getString("Source");
            if(data == null){
                driverAcceptPackage(savedInstanceState);
//                Intent intent = new Intent(getApplicationContext(), AcceptPODDialogFragmentActivity.class);
//                intent.putExtra("pickup", true);
//                startActivity(intent);
                makePopup();
            }
            else if (data.equals("from CaptureSignature")){
                driverAcceptPackage(savedInstanceState);
            }
        }



    }

    private void makePopup() {
        AlertDialog.Builder popup = new AlertDialog.Builder(this);
        popup.setTitle("Accept?")
                .setPositiveButton("Accept", (DialogInterface dialog, int which) -> {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        intent = new Intent(this, CaptureSignature.class);
                        intent.putExtra("pickup", true);
                    }
                    try {
                        buildRequest();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Decline", (dialog, which) -> {

                });
        AlertDialog alertDialog = popup.create();
        alertDialog.show();
    }

    private void buildRequest() throws IOException {

        URL url = new URL(API + "/api/v1/dondepod/mark_that_driver_accepted_shipment");
        HttpURLConnection connection = buildConnection(url);
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
    }


    @NonNull
    private HttpURLConnection buildConnection(URL apiUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("donde_pod_code", ToolBarSetup.DONDEPOD_CODE);
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }


    private void driverAcceptPackage(Bundle savedInstanceState) {
        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        setContentView(R.layout.scanned);
        destination = getDestination();
        origin = getOrigin();
        setupGui(destination, origin);
        LocationServices.getGeofencingClient(this);

        if (destination == null) {
            handleNullDestination();
        }
    }

    @Override
    protected void onNewIntent (Intent intent){
     setIntent(intent);

    }
    private void handleNullDestination() {
        Toast.makeText(getApplicationContext(), "Invaild Barcode", Toast.LENGTH_LONG).show();
        finish();
    }

    @NonNull
    private Destination getDestination() {
        return (Destination) getIntent().getSerializableExtra("Destination");
    }

    private Origin getOrigin() {
        Origin origin;
        origin = (Origin) getIntent().getSerializableExtra("Origin");
        return origin;
    }

    private void setupGui(Destination temp, Origin origin) {
        TextView originName;
        originName =  findViewById(R.id.companyPickup);
        TextView originNumber =  findViewById(R.id.pickupAddress);
        TextView originStreet =  findViewById(R.id.pickupNumber);
        TextView destinationName = this.findViewById(R.id.deliveryCompany);
        TextView destinationStreet =  findViewById(R.id.deliveryStreet);
        TextView destinationNumber = findViewById(R.id.deliveryNumber);
        originName.setText(origin.getCompany());
        originStreet.setText(origin.getAddress());
        originNumber.setText(String.format("%s\t%s", origin.getPhone(), origin.getName()));
        destinationName.setText(temp.getCompany());
        destinationStreet.setText(temp.getAddress());
        destinationNumber.setText(String.format("%s %s", temp.getPhone(), temp.getName()));
        ToolBarSetup.setupToolBar(this, R.id.my_child_toolbar);
        Button button = findViewById(R.id.sendDriver);
        button.setOnClickListener( new ViewOnClickListener() );
    }

    private void startUserService() {
        Log.v(TAG, "Starting LocationService");
        Intent userServiceIntent = new Intent(this, LocationService.class);
        userServiceIntent.putExtra("destination", destination);
        userServiceIntent.putExtra("origin", origin);
        userServiceIntent.putExtra("bol_number", (String) getIntent().getSerializableExtra("bol_number"));
        startService(userServiceIntent);
        Log.v(TAG, "Finishing Start of LocationService");
    }





    private void preventLogMessages() {
        Location location = new Location("");
        location.setLatitude(53.874384);
        location.setLongitude(10.684057);
        locationPlugin.forceLocationUpdate(location);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {

        } else {
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {

            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //required throwaway method
    }

    @Override
    public void onConnectionSuspended(int i) {
        //required throwaway method
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //required throwaway method
    }








        @NonNull
        private LocationRequest getLocationRequest() {
            return LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(1000);
        }

        class ViewOnClickListener implements View.OnClickListener {

        @SuppressLint("MissingPermission")
        public void onClick(View v) {
           ;

            locationRequest = getLocationRequest();

            if ( ! PermissionsManager.areLocationPermissionsGranted(MapLookup.this)) {
                permissionsManager = new PermissionsManager(MapLookup.this);
                permissionsManager.requestLocationPermissions(MapLookup.this);
            }


            Log.v(TAG, "About to start Location Service");
            startUserService();



        }
    }
}




