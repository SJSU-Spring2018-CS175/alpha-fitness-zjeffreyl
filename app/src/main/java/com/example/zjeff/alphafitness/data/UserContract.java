package com.example.zjeff.alphafitness.data;

import android.provider.BaseColumns;

public final class UserContract {

    private UserContract(){}

    public static final class UserEntry implements BaseColumns{
        public static final String TABLE_NAME = "user";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_DISTANCE_AVERAGE = "distanceAverage";
        public static final String COLUMN_TIME_AVERAGE = "timeAverage";
        public static final String COLUMN_WORKOUTS_AVERAGE = "workoutsAverage";
        public static final String COLUMN_CALORIES_AVERAGE = "caloriesAverage";
        public static final String COLUMN_DISTANCE_ALL_TIME = "distanceAllTime";
        public static final String COLUMN_TIME_ALL_TIME = "timeAllTime";
        public static final String COLUMN_WORKOUTS_ALL_TIME = "workoutsAllTime";
        public static final String COLUMN_CALORIES_ALL_TIME = "caloriesAllTime";
    }

}
