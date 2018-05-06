package com.example.zjeff.alphafitness.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "User.db";

    public UserDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " ("
                + UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserContract.UserEntry.COLUMN_NAME + " TEXT, "
                + UserContract.UserEntry.COLUMN_WEIGHT + " INTEGER, "
                + UserContract.UserEntry.COLUMN_DISTANCE_AVERAGE + " INTEGER, "
                + UserContract.UserEntry.COLUMN_TIME_AVERAGE + " INTEGER, "
                + UserContract.UserEntry.COLUMN_WORKOUTS_AVERAGE + " INTEGER, "
                + UserContract.UserEntry.COLUMN_CALORIES_AVERAGE + " INTEGER, "
                + UserContract.UserEntry.COLUMN_DISTANCE_ALL_TIME + " INTEGER, "
                + UserContract.UserEntry.COLUMN_TIME_ALL_TIME + " INTEGER, "
                + UserContract.UserEntry.COLUMN_WORKOUTS_ALL_TIME + " INTEGER, "
                + UserContract.UserEntry.COLUMN_CALORIES_ALL_TIME + " INTEGER); ";
        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
