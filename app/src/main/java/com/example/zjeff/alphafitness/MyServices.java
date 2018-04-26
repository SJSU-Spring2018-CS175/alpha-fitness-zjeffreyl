package com.example.zjeff.alphafitness;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.text.DecimalFormat;
// implements SensorEventListener
public class MyServices extends Service{
    IMyAidlInterface.Stub binder;

    int MilliSeconds, Seconds, Minutes;
    long MillisecondTime, StartTime, UpdateTime, TimeBuff = 0L;
    SensorManager sensorManager;
    public boolean update = false;
    public int stepsTaken = 0;
    public float distance = 0f;
    float stepToMeters = 0.7f;
    float caloriesBurned = 0;
    float caloriesPerStep = 0.04f;


    public static int beginStates;

    /*public MyServices() {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
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
                //MapsActivity.handlerDistance.postDelayed(runnable, 10);
            }

            @Override
            public void stopTime() throws RemoteException {
                MapsActivity.handler.removeCallbacks(runnable);
                //MapsActivity.handlerDistance.removeCallbacks(runnable);
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
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
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
            String duration = (String.format("" + String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds)
                    + ":" + String.format("%02d", MilliSeconds)));
            Message msg = new Message();
            msg.obj = duration;
            MapsActivity.handler.sendMessage(msg);
            MapsActivity.handler.postDelayed(this, 0);

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

    /*@Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        stepsTaken += sensorEvent.values[0];
        caloriesBurned += sensorEvent.values[0] * caloriesPerStep;
        Toast.makeText(this,"" + caloriesBurned, Toast.LENGTH_SHORT).show();
        distance += sensorEvent.values[0] * stepToMeters;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }*/
}
