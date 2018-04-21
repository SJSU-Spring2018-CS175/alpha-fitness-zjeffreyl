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

import com.example.zjeff.alphafitness.data.UserContract;
import com.example.zjeff.alphafitness.data.UserDBHelper;

import org.w3c.dom.Text;

public class Profile extends AppCompatActivity {

    Button goBack;
    //TextView averageDistance = (TextView)findViewById(R.id.Distance);
    //TextView averageTime = (TextView)findViewById(R.id.Time);
    //TextView averageWorkouts = (TextView)findViewById(R.id.Workouts);
    //TextView averageCalories = (TextView)findViewById(R.id.CaloriesBurned);
    //TextView allTimeDistance = (TextView)findViewById(R.id.DistanceAllTime);
    //TextView allTimeTime = (TextView)findViewById(R.id.TimeAllTime);
    //TextView allTimeWorkouts = (TextView)findViewById(R.id.WorkoutsAllTime);
    //TextView allTimeCalories = (TextView)findViewById(R.id.CaloriesBurnedAllTime);

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

        displayDatabaseInfo();
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

            TextView displayView = (TextView) findViewById(R.id.DistanceAllTime);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

    }
}
