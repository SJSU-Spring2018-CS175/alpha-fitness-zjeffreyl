package com.example.zjeff.alphafitness.data;

import android.provider.BaseColumns;

public final class UserContract {
    private UserContract(){}
    public static final class UserEntry implements BaseColumns{
        public final static String TABLE_NAME = "user";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_WEIGHT = "weight";
        public final static String COLUMN_DISTANCE_AVERAGE = "distanceAverage";
        public final static String COLUMN_TIME_AVERAGE = "timeAverage";
        public final static String COLUMN_WORKOUTS_AVERAGE = "workoutsAverage";
        public final static String COLUMN_CALORIES_AVERAGE = "caloriesAverage";
        public final static String COLUMN_DISTANCE_ALL_TIME = "distanceAllTime";
        public final static String COLUMN_TIME_ALL_TIME = "timeAllTime";
        public final static String COLUMN_WORKOUTS_ALL_TIME = "workoutsAllTime";
        public final static String COLUMN_CALORIES_ALL_TIME = "caloriesAllTime";

    }
}
