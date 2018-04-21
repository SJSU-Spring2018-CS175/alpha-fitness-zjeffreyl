package com.example.zjeff.alphafitness;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjeff.alphafitness.data.UserContract;
import com.example.zjeff.alphafitness.data.UserDBHelper;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SensorEventListener {

    private GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 23f;
    public boolean update = false;
    TextView workoutHeading;

    private ArrayList<LatLng> coordinates = new ArrayList<>();
    SensorManager sensorManager;
    boolean running = false;

    Button recordButton;
    ImageButton profileButton;
    //Stopwatch
    TextView duration;
    int MilliSeconds, Seconds, Minutes;
    long MillisecondTime, StartTime, UpdateTime, TimeBuff = 0L;
    public static Handler handler;
    //Distance
    TextView distanceUI;

    //Data
    public int stepsTaken = 0;
    public float distance = 0f;
    float stepToMeters = 0.7f;
    float caloriesBurned = 0;
    float caloriesPerStep = 0.04f;

    private UserDBHelper userDBHelper;

    //service connection
    IMyAidlInterface remoteService;
    RemoteConnection remoteConnection = null;

    class RemoteConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = IMyAidlInterface.Stub.asInterface((IBinder) service);
            Toast.makeText(MapsActivity.this,
                    "Remote Service connected.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteService = null;
            Toast.makeText(MapsActivity.this,
                    "Remote Service disconnected.", Toast.LENGTH_LONG).show();
        }
    }

    LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getLocationPermission();
        userDBHelper = new UserDBHelper(this);
        workoutHeading = (TextView)findViewById(R.id.recordworkoutheading);
        profileButton = (ImageButton) findViewById(R.id.profile);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        // initialize the service
        remoteConnection = new RemoteConnection();
        Intent intent = new Intent();
        intent.setClassName("com.example.zjeff.alphafitness",
               com.example.zjeff.alphafitness.MyServices.class.getName());
        if (!bindService(intent, remoteConnection, BIND_AUTO_CREATE)) {
            Toast.makeText(MapsActivity.this,
                    "Fail to bind the remote service.", Toast.LENGTH_LONG).show();
        }

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
                    //TimeBuff += MillisecondTime;
                    handler.removeCallbacks(runnable);
                    running = false;
                    //Option to Reset
                    recordButton.setText("Reset");
                    state = recordState.STOP;
                } else
                    //Stop goes to rest
                    if (state == recordState.STOP) {
                        //Everything that happens at start here
                        clearDataValues();
                        MillisecondTime = 0L;
                        Seconds = 0;
                        Minutes = 0;
                        MilliSeconds = 0;
                        mMap.clear();
                        duration.setText("00:00:00");
                        distanceUI.setText("0.00M");
                        //Option to Start
                        recordButton.setText("Start");
                        state = recordState.REST;
                    }
                    //Rest goes to start
                    else {
                        StartTime = SystemClock.uptimeMillis();
                        handler.postDelayed(runnable, 0);
                        running = true;
                        //Option to stop
                        recordButton.setText("Stop");
                        //lerp camera lock
                        state = recordState.START;
                    }
            }
        });
        duration = (TextView) findViewById(R.id.duration);
        distanceUI = (TextView) findViewById(R.id.distance);
        handler = new Handler();

        displayDatabaseInfo();
    }


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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(running){
            stepsTaken += sensorEvent.values[0];
            caloriesBurned += sensorEvent.values[0] * caloriesPerStep;
            Toast.makeText(this,"" + caloriesBurned, Toast.LENGTH_SHORT).show();
            distance += sensorEvent.values[0] * stepToMeters;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //record states
    public enum recordState {
        START, STOP, REST
    }

    ;
    public recordState state = recordState.REST;

    @Override
    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_SHORT).show();
        }
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
            //roundedDistance 0.00
            DecimalFormat f = new DecimalFormat("##.0");
            distanceUI.setText("" + f.format(distance) + "m");
            handler.postDelayed(this, 0);
        }
    };

    private void drawPolyLine(){
        LatLng src =  new LatLng(coordinates.get(coordinates.size()-4).latitude, coordinates.get(coordinates.size()-4).longitude);
        LatLng des =  new LatLng(coordinates.get(coordinates.size()-1).latitude, coordinates.get(coordinates.size()-1).longitude);
        Polyline line = mMap.addPolyline(new PolylineOptions().add(src,des).width(3).color(Color.BLUE).geodesic(true));
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
        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), 17f);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(state != recordState.START){
            if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
                /*LandscapeRecordWorkout landscapeRecordWorkout = new LandscapeRecordWorkout();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(android.R.id.content, landscapeRecordWorkout).commit();*/
                startActivity(new Intent(this, LandscapeRecordWorkout.class));
            }
        }
    }

    public void clearDataValues(){
        coordinates.clear();
        stepsTaken =0;
        distance = 0;
        caloriesBurned = 0;
    }

    public void insertUserData(){
        SQLiteDatabase db = userDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NAME, "Jeff");
        values.put(UserContract.UserEntry.COLUMN_WEIGHT, "160");
        values.put(UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE, "30");
        values.put(UserContract.UserEntry.COLUMN_TIME_AVERAGE,"20000");
        values.put(UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE,"1");
        values.put(UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, "50");
        values.put(UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME, "100");
        values.put(UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME, "3");
        values.put(UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, "120");
        db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_dummy_data:
                if(state == recordState.STOP){
                    insertUserData();
                }
                return true;
            case R.id.action_delete_all_entries:
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        UserDBHelper mDbHelper = new UserDBHelper(this);
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserContract.UserEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.recordworkoutheading);
            displayView.setText("Number of rows in user database table: " + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }


}
