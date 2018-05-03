package com.example.zjeff.alphafitness;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class LandscapeRecordWorkout extends AppCompatActivity {


    public LandscapeRecordWorkout() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LandscapeRecord", "Start Landscape");
        setContentView(R.layout.fragment_landscape_record_workout);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("LandscapeRecord", "orientation sensed");
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d("LandscapeRecord", "orientation changing");
            startActivity(new Intent(this, MapsActivity.class));
        }
    }
}
