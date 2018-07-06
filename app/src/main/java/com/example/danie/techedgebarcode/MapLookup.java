package com.example.danie.techedgebarcode;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
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
import com.google.android.gms.location.Geofence;
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


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Daniel Menard on 1/25/2018.
 */

public class MapLookup extends AppCompatActivity implements LocationEngineListener, PermissionsListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "DirectionsActivity";
    private MapView map;
    private MapboxMap mMap;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private Marker destinationMarker;
    private Point originPosition;
    private Point destinationPosition;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    private Geofence geofence;
    private GeofencingClient mGeofencingClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Destination destination;
    private Origin origin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.scanned);
        map = (MapView) findViewById(R.id.mapView);
        destination = getDestination();
        origin = getOrigin();
        setupGui(destination, origin);
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        if (destination == null) {
            handleNullDestination();
        } else {
            map.onCreate(savedInstanceState);
            map.getMapAsync(new CustomOnMapReadyCallback(origin, destination) );
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
        Origin origin = null;
        origin = (Origin) getIntent().getSerializableExtra("Origin");
        return origin;
    }

    private void setupGui(Destination temp, Origin origin) {
        TextView originName;
        originName = (TextView) findViewById(R.id.companyPickup);
        TextView originNumber = (TextView) findViewById(R.id.pickupAddress);
        TextView originStreet = (TextView) findViewById(R.id.pickupNumber);
        TextView destinationName = (TextView) this.<View>findViewById(R.id.deliveryCompany);
        TextView destinationStreet = (TextView) findViewById(R.id.deliveryStreet);
        TextView destinationNumber = (TextView) findViewById(R.id.deliveryNumber);
        originName.setText(origin.getCompany());
        originStreet.setText(origin.getAddress());
        originNumber.setText(String.format("%s\t%s", origin.getPhone(), origin.getName()));
        destinationName.setText(temp.getCompany());
        destinationStreet.setText(temp.getAddress());
        destinationNumber.setText(String.format("%s %s", temp.getPhone(), temp.getName()));
        ToolBarSetup.setupToolBar(this, R.id.my_child_toolbar);
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


    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new DirectionsResponseCallback());
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(map, mMap, locationEngine);
            preventLogMessages();
            locationPlugin.setLocationLayerEnabled(true);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void preventLogMessages() {
        Location location = new Location("");
        location.setLatitude(53.874384);
        location.setLongitude(10.684057);
        locationPlugin.forceLocationUpdate(location);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();


        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location) {
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
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
            enableLocationPlugin();
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
            originLocation = location;
            setCameraPosition(location);
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
        map.onStart();
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
        map.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

// gets the lat and lng from a location
    public LatLng getLatLongFromPlace(String place) {
        try {
            Geocoder selected_place_geocoder = new Geocoder(getApplicationContext());
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);

            if (address != null) {
                Address location = address.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    class DirectionsResponseCallback implements Callback<DirectionsResponse> {
        @Override
        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
            Log.d(TAG, "Response code: " + response.code());

            if (handleRouteProblems(response)) {
                return;
            }

            currentRoute = response.body().routes().get(0);
            drawRouteOnMap();
        }

        private void drawRouteOnMap() {
            if (navigationMapRoute != null) {
                navigationMapRoute.removeRoute();
            } else {
                Log.v(TAG, "mMap:"+mMap);
                navigationMapRoute = new NavigationMapRoute(null, map, mMap, R.style.NavigationMapRoute);
            }
            navigationMapRoute.addRoute(currentRoute);
        }

        private boolean handleRouteProblems(Response<DirectionsResponse> response) {
            if (response.body() == null) {
                Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                return true;
            } else if (response.body().routes().size() < 1) {
                Log.e(TAG, "No routes found");
                return true;
            }
            return false;
        }

        @Override
        public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
            Log.e(TAG, "Error: " + throwable.getMessage());
        }
    }

    class CustomOnMapReadyCallback implements OnMapReadyCallback {

        private Destination destination;
        private Origin origin;

        public CustomOnMapReadyCallback(Origin origin, Destination destination) {
            this.destination = destination;
            this.origin = origin;
        }

        @Override
        public void onMapReady(final MapboxMap mapboxMap) {
            mMap = mapboxMap;
            enableLocationPlugin();

            LatLng originCoord = getLatLongFromPlace(origin.getAddress());
            LatLng destinationCoord = getLatLongFromPlace(destination.getAddress());

            if(destinationCoord != null && originCoord != null){
                destinationMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(destinationCoord)
                );
                destinationPosition = Point.fromLngLat(destinationCoord.getLongitude(), destinationCoord.getLatitude());

                originPosition = Point.fromLngLat(originCoord.getLongitude(), originCoord.getLatitude());
                getRoute(originPosition, destinationPosition);
                Button button = (Button) findViewById(R.id.sendDriver);
                button.setOnClickListener( new ViewOnClickListener() );
                Button settingBtn = (Button) findViewById(R.id.settings);
                settingBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent settings = new Intent(MapLookup.this, Settings.class);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Error missing Coordinates", Toast.LENGTH_LONG).show();
            }

        }

        private NavigationLauncherOptions setupNavLauncherOptions(Point origin, Point destination) {
            return NavigationLauncherOptions.builder()
                    .origin(origin)
                    .destination(destination)
                    .shouldSimulateRoute(true)
                    .build();
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
//            Point origin = originPosition;
//            Point destination = destinationPosition;
//
//            locationRequest = getLocationRequest();
//
//            if ( ! PermissionsManager.areLocationPermissionsGranted(MapLookup.this)) {
//                permissionsManager = new PermissionsManager(MapLookup.this);
//                permissionsManager.requestLocationPermissions(MapLookup.this);
//            }
//
//
//            Log.v(TAG, "About to start Location Service");
//            startUserService();
            Intent intent = new Intent(getApplicationContext(), CaptureSignature.class);
            startActivity(intent);


        }
    }
}


}

