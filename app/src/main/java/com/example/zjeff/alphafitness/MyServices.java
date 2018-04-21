package com.example.zjeff.alphafitness;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class MyServices extends Service {
    IMyAidlInterface.Stub binder;

    public MyServices() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new IMyAidlInterface.Stub() {
            @Override
            public void startTime() throws RemoteException {

            }

            @Override
            public void stopTime() throws RemoteException {

            }

            @Override
            public void restTime() throws RemoteException {

            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
