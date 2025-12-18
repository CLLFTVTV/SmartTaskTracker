package com.example.smarttasktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SmartTaskDB_v2";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "tasks";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_COST = "cost";
    private static final String COL_EXPECTED_TIME = "expected_time"; // minutes
    private static final String COL_ACTUAL_TIME = "actual_time"; // seconds
    private static final String COL_START_TIMESTAMP = "start_timestamp"; // milliseconds
    private static final String COL_STATUS = "status"; // 0=Pending, 1=Running, 2=Completed

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_COST + " REAL, " +
                COL_EXPECTED_TIME + " INTEGER, " +
                COL_ACTUAL_TIME + " INTEGER, " +
                COL_START_TIMESTAMP + " INTEGER, " +
                COL_STATUS + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addTask(String name, double cost, int expectedMinutes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_COST, cost);
        values.put(COL_EXPECTED_TIME, expectedMinutes);
        values.put(COL_ACTUAL_TIME, 0);
        values.put(COL_START_TIMESTAMP, 0);
        values.put(COL_STATUS, 0); // Pending
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Start the Timer
    public void startTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_START_TIMESTAMP, System.currentTimeMillis());
        values.put(COL_STATUS, 1); // Running
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Stop the Timer (Complete the task)
    public void completeTask(int id, long elapsedSeconds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ACTUAL_TIME, elapsedSeconds);
        values.put(COL_STATUS, 2); // Completed
        db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // sortMode: 0 = Name (A-Z), 1 = Cost (High-Low), 2 = Expected Time (Short-Long)
    // sortMode: 0 = Name (A-Z), 1 = Cost (High-Low), 2 = Expected Time (Short-Long)
    public List<Task> getTasks(boolean isCompleted, int sortMode) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = isCompleted ? COL_STATUS + "=2" : COL_STATUS + "!=2";
        String orderBy;

        switch (sortMode) {
            case 1: // Cost: High to Low
                orderBy = COL_COST + " DESC";
                break;
            case 2: // Time: Shortest First
                orderBy = COL_EXPECTED_TIME + " ASC";
                break;
            default: // Name: A to Z (Case Insensitive FIX)
                orderBy = COL_NAME + " COLLATE NOCASE ASC";
                break;
        }

        Cursor cursor = db.query(TABLE_NAME, null, selection, null, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                taskList.add(new Task(
                        cursor.getInt(0), // id
                        cursor.getString(1), // name
                        cursor.getDouble(2), // cost
                        cursor.getInt(3), // expected
                        cursor.getLong(4), // actual
                        cursor.getLong(5), // start timestamp
                        cursor.getInt(6)   // status
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }
}