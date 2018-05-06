package com.example.zjeff.alphafitness;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.zjeff.alphafitness.data.UserContract;
import com.example.zjeff.alphafitness.data.UserDBHelper;
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

public class MyServices extends Service implements OnMapReadyCallback, SensorEventListener{
    IMyAidlInterface.Stub binder;

    SensorManager sensorManager;
    Sensor countSensor;

    private GoogleMap mMap;
    int MilliSeconds, Seconds, Minutes;
    long MillisecondTime, StartTime, UpdateTime, TimeBuff = 0L;
    public boolean update = false;

    public int stepsTaken = 0;
    public float distance = 0f;
    float stepToMeters = 0.7f;
    float caloriesBurned = 0;
    float caloriesPerStep = 4f;

    //Data
    float workoutsAverage;
    float workoutsAllTime;

    LocationManager locationManager;
    Location location;
    private static final float DEFAULT_ZOOM = 20f;
    public static ArrayList<LatLng> coordinates = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static int beginStates;
    private UserDBHelper userDBHelper;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.values[0] == 1 && beginStates == 1){
            stepsTaken++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //record states
    public enum recordState {
        START, STOP, REST
    }

    public MapsActivity.recordState state = MapsActivity.recordState.REST;

    @Override
    public void onCreate() {
        super.onCreate();
        userDBHelper = new UserDBHelper(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        binder = new IMyAidlInterface.Stub() {
            @Override
            public void startTime() throws RemoteException {
                StartTime = SystemClock.uptimeMillis();
                beginStates = 1;
                countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                if (countSensor != null) {
                    sensorManager.registerListener(MyServices.this, countSensor, SensorManager.SENSOR_DELAY_UI);
                } else {
                    Toast.makeText(MyServices.this, "Sensor not found", Toast.LENGTH_SHORT).show();
                }
                MapsActivity.handler.postDelayed(runnable, 10);
                MapsActivity.GoogleMapHandler.postDelayed(polyLine, 10);
            }

            @Override
            public void stopTime() throws RemoteException {
                insertUserData();
                MapsActivity.handler.removeCallbacks(runnable);
                MapsActivity.GoogleMapHandler.removeCallbacks(polyLine);
                beginStates = 2;
            }

            @Override
            public void restTime() throws RemoteException {
                //Time
                MillisecondTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;
                //Distance
                stepsTaken = 0;
                distance = 0f;
                coordinates.clear();
                beginStates = 0;
            }
        };
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

            Message stepsTakenMessage = new Message();
            float distance = stepsTaken * stepToMeters;
            distance *= 0.0006213;
            stepsTakenMessage.obj = distance;
            MapsActivity.distanceHandler.sendMessage(stepsTakenMessage);
        }
    };

    Runnable polyLine = new Runnable() {
        @Override
        public void run(){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            String locationProvider = locationManager.getBestProvider(criteria, false);


            if (ActivityCompat.checkSelfPermission(MyServices.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MyServices.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }

            location = locationManager.getLastKnownLocation(locationProvider);
            if(location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //coordinates.add(latLng);
                Message locationMsg = new Message();
                locationMsg.obj = coordinates;
                MapsActivity.GoogleMapHandler.sendMessage(locationMsg);
                MapsActivity.GoogleMapHandler.postDelayed(this, 10);
            }

        }
    };

    public void insertUserData(){
        float currentDistanceAverage = 0;
        float currentTimeAverage = 0;
        float currentWorkoutsAverage = 0;
        float currentCaloriesAverage = 0;

        float currentDistanceAllTime = 0;
        float currentTimeAllTime = 0;
        float currentWorkoutsAllTime = 0;
        float currentCaloriesAllTime = 0;
        int currentId = 0;
        //Get Data Values
        UserDBHelper mDbHelper = new UserDBHelper(this);
        SQLiteDatabase rdb = mDbHelper.getReadableDatabase();

        String[] projection = {UserContract.UserEntry._ID, UserContract.UserEntry.COLUMN_NAME, UserContract.UserEntry.COLUMN_WEIGHT,
                UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE, UserContract.UserEntry.COLUMN_TIME_AVERAGE, UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE,
                UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME, UserContract.UserEntry.COLUMN_TIME_ALL_TIME,
                UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME, UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME};

        Cursor cursor = rdb.query(UserContract.UserEntry.TABLE_NAME, projection, null, null, null, null, null);
        try {
            int idColumnIndex = cursor.getColumnIndex(UserContract.UserEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME);
            int weightColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WEIGHT);

            int distanceAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE);
            int timeAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_TIME_AVERAGE);
            int workoutsAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE);
            int caloriesAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_CALORIES_AVERAGE);
            //index of column for all time
            int distanceAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME);
            int timeAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_TIME_ALL_TIME);
            int workoutAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME);
            int caloriesAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME);
            cursor.moveToFirst();

            workoutsAverage++;
            workoutsAllTime = workoutsAverage;
            while(cursor.moveToNext()) {
                currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                //retrieving sum
                currentDistanceAverage += cursor.getFloat(distanceAverageColumnIndex);
                currentTimeAverage += cursor.getFloat(timeAverageColumnIndex);
                currentWorkoutsAverage += cursor.getFloat(workoutsAverageColumnIndex);
                currentCaloriesAverage += cursor.getFloat(caloriesAverageColumnIndex);

                currentDistanceAllTime = cursor.getFloat(distanceAllTimeColumnIndex);
                currentTimeAllTime = cursor.getFloat(timeAllTimeColumnIndex);
                currentCaloriesAllTime = cursor.getFloat(caloriesAllTimeColumnIndex);
                currentWorkoutsAllTime = cursor.getFloat(workoutAllTimeColumnIndex);
            }
            Log.d("AAA", "" + currentId + 1);
            //calculating new data and divide to get average
            currentDistanceAverage = (currentDistanceAverage + (stepsTaken * stepToMeters))/(currentId + 1);
            currentTimeAverage = (currentTimeAverage + Seconds)/(currentId + 1);
            currentWorkoutsAverage = (currentWorkoutsAverage + workoutsAverage)/(currentId + 1);
            currentCaloriesAverage = (currentCaloriesAverage + (stepsTaken * caloriesPerStep))/(currentId + 1);

            if(stepsTaken * stepToMeters > currentDistanceAllTime){
                currentDistanceAllTime = stepsTaken * stepToMeters;
            }
            if(Seconds > currentTimeAllTime){
                currentTimeAllTime = Seconds;
            }
            if(stepsTaken * caloriesPerStep > currentCaloriesAllTime){
                currentCaloriesAllTime = stepsTaken * caloriesPerStep;
            }
            if(workoutsAllTime > currentWorkoutsAllTime){
                currentDistanceAllTime = workoutsAllTime;
            }

        }finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

        SQLiteDatabase db = userDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NAME, "Jeff");
        values.put(UserContract.UserEntry.COLUMN_WEIGHT, "160");
        values.put(UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE, currentDistanceAverage);
        values.put(UserContract.UserEntry.COLUMN_TIME_AVERAGE, currentTimeAverage);
        values.put(UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE,currentWorkoutsAverage);
        values.put(UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, currentCaloriesAverage);
        values.put(UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME, currentDistanceAllTime);
        values.put(UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME, currentWorkoutsAllTime);
        values.put(UserContract.UserEntry.COLUMN_TIME_ALL_TIME, currentTimeAllTime);
        values.put(UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME, currentCaloriesAllTime);
        db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
    }

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
                startActivity(new Intent(this, LandscapeRecordWorkout.class));
            }
        }
    }
}
