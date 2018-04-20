package com.example.zjeff.alphafitness;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 23f;
    public boolean update = false;

    private ArrayList<LatLng> coordinates = new ArrayList<>();
    public float distanceInMeters = 0;

    Button recordButton;
    ImageButton profileButton;
    //Stopwatch
    TextView duration;
    int MilliSeconds, Seconds, Minutes;
    long MillisecondTime, StartTime, UpdateTime, TimeBuff = 0L;
    Handler handler;


    LocationManager locationManager;

    @Override
    public void onLocationChanged(Location location) {
        if(state == recordState.START) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            moveCamera(latLng, DEFAULT_ZOOM);
            coordinates.add(latLng);
            update = true;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //record states
    public enum recordState {
        START, STOP, REST
    }

    ;
    public recordState state = recordState.REST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getLocationPermission();
        profileButton = (ImageButton) findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MapsActivity.this, Profile.class);
                startActivity(i);
            }
        });
        //Stopwatch
        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start goes to stop
                if (state == recordState.START) {
                    //Everything that happens at stop here
                    TimeBuff += MillisecondTime;
                    handler.removeCallbacks(runnable);
                    //Option to Reset
                    recordButton.setText("Reset");
                    state = recordState.STOP;
                } else
                    //Stop goes to rest
                    if (state == recordState.STOP) {
                        //Everything that happens at start here
                        MillisecondTime = 0L;
                        Seconds = 0;
                        Minutes = 0;
                        MilliSeconds = 0;
                        duration.setText("00:00:00");
                        Toast.makeText(getApplicationContext(), "" + distanceInMeters + " m of size: " + coordinates.size(), Toast.LENGTH_SHORT).show();
                        coordinates.clear();
                        mMap.clear();
                        distanceInMeters = 0;
                        //Option to Start
                        recordButton.setText("Start");
                        state = recordState.REST;
                    }
                    //Rest goes to start
                    else {
                        StartTime = SystemClock.uptimeMillis();
                        handler.postDelayed(runnable, 0);
                        //Option to stop
                        recordButton.setText("Stop");
                        //lerp camera lock
                        state = recordState.START;
                    }
            }
        });
        duration = (TextView) findViewById(R.id.duration);

        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE && state == recordState.STOP){
            LandscapeRecordWorkout landscapeRecordWorkout = new LandscapeRecordWorkout();
            fragmentTransaction.replace(android.R.id.content, landscapeRecordWorkout).commit();
        }
        handler = new Handler();
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 100);
            if(update = true && state == recordState.START && coordinates.size() % 3 == 0){
                if(coordinates.size() > 3) {
                    drawPolyLine();
                }
                update = false;
            }
            duration.setText(String.format("" + String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds)
                    + ":" + String.format("%02d", MilliSeconds)));
            handler.postDelayed(this, 0);
        }
    };

    private void drawPolyLine(){
        LatLng src =  new LatLng(coordinates.get(coordinates.size()-4).latitude, coordinates.get(coordinates.size()-4).longitude);
        LatLng des =  new LatLng(coordinates.get(coordinates.size()-1).latitude, coordinates.get(coordinates.size()-1).longitude);
        Polyline line = mMap.addPolyline(new PolylineOptions().add(src,des).width(3).color(Color.BLUE).geodesic(true));
        Location loc1 = new Location("");
        loc1.setLatitude(src.latitude);
        loc1.setLongitude(src.longitude);
        Location loc2 = new Location("");
        loc2.setLatitude(des.latitude);
        loc2.setLongitude(des.longitude);
        distanceInMeters += loc1.distanceTo(loc2);
    }

    private void getDeviceLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
        locationManager.requestLocationUpdates(provider, 1000, 0, this);
    }

    private void drawMarker(Location location){
        mMap.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions().position(currentPosition).snippet("Lat: " + location.getLatitude() + " Lng: " + location.getLongitude()));
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        final SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(this, "onRequestPermissionsResult", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready HERE", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        Log.d(TAG, "" + mLocationPermissionGranted);
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            //mark location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }
}
