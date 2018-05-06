package com.example.zjeff.alphafitness;


import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zjeff.alphafitness.data.UserContract;
import com.example.zjeff.alphafitness.data.UserDBHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class LandscapeRecordWorkout extends AppCompatActivity implements OnChartValueSelectedListener, OnChartGestureListener {
    private LineChart mChart;


    public LandscapeRecordWorkout() {
        // Required empty public constructor
    }

    TextView averageMinPerMile;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_landscape_record_workout);
        averageMinPerMile = (TextView) findViewById(R.id.avgValue);
        int minutes = (int)(getAverageTimeData()/0.0006213)/60;
        int seconds = (int)(getAverageTimeData()/0.0006213)%60;
        averageMinPerMile.setText(String.format("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds)));
        mChart = (LineChart) findViewById(R.id.chart);

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(true);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setDrawGridLines(false);
        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        ArrayList<Entry> caloriesValues = new ArrayList<>();
        caloriesValues.add(new Entry(0,0f));
        caloriesValues.add(new Entry(1,2f));
        caloriesValues.add(new Entry(2,2f));


        ArrayList<Entry> stepValues = new ArrayList<>();
        stepValues.add(new Entry(0,0f));
        stepValues.add(new Entry(1,24f));
        stepValues.add(new Entry(2,24f));


        LineDataSet set1 = createCaloriesSet(caloriesValues);
        LineDataSet set2 = createStepsSet(stepValues);
        ArrayList<ILineDataSet> dataset1 = new ArrayList<>();
        dataset1.add(set1);
        dataset1.add(set2);
        LineData line = new LineData(dataset1);

        mChart.setData(line);

        updateChartUI(2);
        updateChartUI(3);
        updateChartUI(2);
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

    public double getAverageTimeData(){
        UserDBHelper mDbHelper = new UserDBHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        double averageTimePerDistance = 0;
        String[] projection = {UserContract.UserEntry._ID, UserContract.UserEntry.COLUMN_NAME, UserContract.UserEntry.COLUMN_WEIGHT,
                UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE, UserContract.UserEntry.COLUMN_TIME_AVERAGE, UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE,
                UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME, UserContract.UserEntry.COLUMN_TIME_ALL_TIME,
                UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME, UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME};
        Cursor cursor = db.query(UserContract.UserEntry.TABLE_NAME, projection, null, null, null, null, null);
        try{
            int idColumnIndex = cursor.getColumnIndex(UserContract.UserEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_NAME);
            int weightColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WEIGHT);

            int distanceAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE);
            int timeAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_TIME_AVERAGE);
            int workoutsAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE);
            int caloriesAverageColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_CALORIES_AVERAGE);

            int distanceAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME);
            int timeAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_TIME_ALL_TIME);
            int workoutAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME);
            int caloriesAllTimeColumnIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME);
            cursor.moveToLast();
            int currentDistanceAverage = cursor.getInt(distanceAverageColumnIndex);
            int currentTimeAverage = cursor.getInt(timeAverageColumnIndex);
            averageTimePerDistance = currentTimeAverage/currentDistanceAverage;
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
        return averageTimePerDistance;
    }

    private LineDataSet createCaloriesSet(ArrayList<Entry> cal) {



        LineDataSet set = new LineDataSet(cal, "Calories Burned");





        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setColor(Color.GREEN);

        set.setCircleColor(Color.BLUE);

        set.setLineWidth(2f);

        set.setCircleRadius(4f);

        set.setFillAlpha(65);

        set.setFillColor(Color.BLUE);

        set.setHighLightColor(Color.rgb(117, 117, 117));

        set.setValueTextColor(Color.BLUE);

        set.setValueTextSize(9f);

        return set;

    }



    private LineDataSet createStepsSet(ArrayList<Entry> st) {

        LineDataSet set = new LineDataSet(st, "Step Counts");

        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setColor(Color.BLUE);

        set.setCircleColor(Color.RED);

        set.setLineWidth(2f);

        set.setCircleRadius(4f);

        set.setFillAlpha(65);

        set.setFillColor(Color.RED);

        set.setHighLightColor(Color.rgb(244, 117, 117));

        set.setValueTextColor(Color.RED);

        set.setValueTextSize(9f);

        return set;

    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void updateChartUI(float caloriesBurned) {
        LineData data = mChart.getData();



        if (data != null) {





            ILineDataSet caloriesSet = data.getDataSetByIndex(0);

            ILineDataSet stepsSet = data.getDataSetByIndex(1);



            //caloriesSet.addEntry(new Entry(3f, 4f));

            int steps = (int) (caloriesBurned/0.05);



            data.addEntry(new Entry(data.getDataSetByIndex(0).getEntryCount()*5, caloriesBurned), 0);

            data.addEntry(new Entry(data.getDataSetByIndex(1).getEntryCount()*5, steps), 1);



            data.notifyDataChanged();



            // let the chart know it's data has changed

            mChart.notifyDataSetChanged();



            // limit the number of visible entries

            mChart.setVisibleXRangeMaximum(10);

            // mChart.setVisibleYRange(30, AxisDependency.LEFT);



            // move to the latest entry

            if (data.getEntryCount() >= 5)

                mChart.moveViewToX(data.getEntryCount()*5);



            // this automatically refreshes the chart (calls invalidate())

            // mChart.moveViewTo(data.getXValCount()-7, 55f,

            // AxisDependency.LEFT);

        }
    }
}
