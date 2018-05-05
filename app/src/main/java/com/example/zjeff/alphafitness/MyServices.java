package com.example.zjeff.alphafitness;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.inputmethod.InputMethod;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MyServices extends Service implements OnMapReadyCallback{
    IMyAidlInterface.Stub binder;

    private GoogleMap mMap;
    int MilliSeconds, Seconds, Minutes;
    long MillisecondTime, StartTime, UpdateTime, TimeBuff = 0L;
    SensorManager sensorManager;
    public boolean update = false;
    public int stepsTaken = 0;
    public float distance = 0f;
    float stepToMeters = 0.7f;
    float caloriesBurned = 0;
    float caloriesPerStep = 0.04f;
    LocationManager locationManager;
    private static final float DEFAULT_ZOOM = 20f;
    private ArrayList<LatLng> coordinates = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static int beginStates;

    //record states
    public enum recordState {
        START, STOP, REST
    }

    public MapsActivity.recordState state = MapsActivity.recordState.REST;

    /*public MyServices() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new IMyAidlInterface.Stub() {
            @Override
            public void startTime() throws RemoteException {
                StartTime = SystemClock.uptimeMillis();
                beginStates = 1;
                MapsActivity.handler.postDelayed(runnable, 10);
            }

            @Override
            public void stopTime() throws RemoteException {
                MapsActivity.handler.removeCallbacks(runnable);
                beginStates = 2;
            }

            @Override
            public void restTime() throws RemoteException {
                MillisecondTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;
                beginStates = 0;
            }
        };
        /*Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_SHORT).show();
        }*/
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 100);
            String duration = (String.format("" + String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds)));

            Message msg = new Message();
            msg.obj = duration;
            MapsActivity.handler.sendMessage(msg);
            MapsActivity.handler.postDelayed(this, 1000);

            /*DecimalFormat f = new DecimalFormat("##.0");
            String distanceUI = "" + f.format(distance) + "m";
            Message msg1 = new Message();
            msg1.obj = distanceUI;
            MapsActivity.handlerDistance.sendMessage(msg1);
            MapsActivity.handlerDistance.postDelayed(this,0);*/
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
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
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        Toast.makeText(MyServices.this, "Location: " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    } else {
                        Toast.makeText(MyServices.this, "unable to provide current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception");
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        getDeviceLocation();
        //mark location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(state != MapsActivity.recordState.START){
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
                /*LandscapeRecordWorkout landscapeRecordWorkout = new LandscapeRecordWorkout();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(android.R.id.content, landscapeRecordWorkout).commit();*/
                startActivity(new Intent(this, LandscapeRecordWorkout.class));
            }
        }
    }
}
