package com.example.zjeff.alphafitness;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zjeff.alphafitness.data.UserContract;
import com.example.zjeff.alphafitness.data.UserDBHelper;

import org.w3c.dom.Text;

public class Profile extends AppCompatActivity {

    Button goBack;
    TextView name;
    TextView weight;
    TextView averageDistance;
    TextView averageTime;
    TextView averageWorkouts;
    TextView averageCalories;
    TextView allTimeDistance;
    TextView allTimeTime;
    TextView allTimeWorkouts;
    TextView allTimeCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        goBack = (Button) findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, MapsActivity.class);
                startActivity(i);
            }
        });
        name = (TextView)findViewById(R.id.name);
        weight = (TextView)findViewById(R.id.weight);
        averageDistance = (TextView)findViewById(R.id.Distance);
        averageTime = (TextView)findViewById(R.id.Time);
        averageWorkouts = (TextView)findViewById(R.id.Workouts);
        averageCalories = (TextView)findViewById(R.id.CaloriesBurned);
        allTimeDistance = (TextView)findViewById(R.id.DistanceAllTime);
        allTimeTime = (TextView)findViewById(R.id.TimeAllTime);
        allTimeWorkouts = (TextView)findViewById(R.id.WorkoutsAllTime);
        allTimeCalories = (TextView)findViewById(R.id.CaloriesBurnedAllTime);
        displayDatabaseInfo();
    }


    private void displayDatabaseInfo() {
        UserDBHelper mDbHelper = new UserDBHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {UserContract.UserEntry._ID, UserContract.UserEntry.COLUMN_NAME, UserContract.UserEntry.COLUMN_WEIGHT,
                UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE, UserContract.UserEntry.COLUMN_TIME_AVERAGE, UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE,
                UserContract.UserEntry.COLUMN_CALORIES_AVERAGE, UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME, UserContract.UserEntry.COLUMN_TIME_ALL_TIME,
                UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME, UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME};

        Cursor cursor = db.query(UserContract.UserEntry.TABLE_NAME, projection, null, null, null, null, null);
        try {
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

            while(cursor.moveToNext()) {
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);

                int currentDistanceAverage = cursor.getInt(distanceAverageColumnIndex);
                int currentTimeAverage = cursor.getInt(timeAverageColumnIndex);
                int currentWorkoutsAverage = cursor.getInt(workoutsAverageColumnIndex);
                int currentCaloriesAverage = cursor.getInt(caloriesAverageColumnIndex);

                int currentDistanceAllTime = cursor.getInt(distanceAllTimeColumnIndex);
                int currentTimeAllTime = cursor.getInt(timeAllTimeColumnIndex);
                int currentWorkoutsAllTime = cursor.getInt(workoutAllTimeColumnIndex);
                int currentCaloriesAllTime = cursor.getInt(caloriesAllTimeColumnIndex);

                name.setText(currentName);
                weight.setText("" + currentWeight);
                TextView gender = (TextView)findViewById(R.id.gender);
                gender.setText("Male");
                averageDistance.setText("" + currentDistanceAverage);
                averageTime.setText("" + currentTimeAverage);
                averageWorkouts.setText("" + currentWorkoutsAverage);
                averageCalories.setText("" + currentCaloriesAverage);

                allTimeDistance.setText("" + currentDistanceAllTime);
                allTimeTime.setText("" + currentTimeAllTime);
                allTimeWorkouts.setText("" + currentWorkoutsAllTime);
                allTimeCalories.setText("" + currentCaloriesAllTime);

            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
}
