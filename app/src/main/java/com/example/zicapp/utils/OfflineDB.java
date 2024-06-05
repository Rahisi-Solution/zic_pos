package com.example.zicapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class OfflineDB extends SQLiteOpenHelper {

    public OfflineDB(@Nullable Context context) {
        super(context, "zic_dev_2", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    String scanned_certificate;
    String failed_certificate;

    // Creating Tables in offline database (SQLite)
    scanned_certificate =  "CREATE TABLE scanned_certificate (reference_number TEXT, date TEXT, time TEXT)";
    failed_certificate =  "CREATE TABLE failed_certificate (reference_number TEXT, date TEXT, time TEXT)";

    // Executing query to Create Table in offline database (SQLite)
    sqLiteDatabase.execSQL(scanned_certificate);
    sqLiteDatabase.execSQL(failed_certificate);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        // Drop Table on the Database Upgrade
        String scannedCertificates = "DROP TABLE IF EXISTS scanned_certificate";
        String failedCertificate = "DROP TABLE IF EXISTS failed_certificate";

        // Executing Drop Table Queries
        sqLiteDatabase.execSQL(scannedCertificates);
        sqLiteDatabase.execSQL(failedCertificate);
    }

    // Inserting Valid Certificate in scanned_certificate Table
    public void insertCertificate(String reference_number, String date, String time){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", reference_number);
            values.put("date", date);
            values.put("time", time);
            System.out.println("Insert Certificate " + values);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        database.insert("scanned_certificate", null, values);
        database.close();
    }

    // Inserting Invalid Certificate in failed_certificate Table
    public void insertInvalidCertificate(String reference_number, String date, String time){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", reference_number);
            values.put("date", date);
            values.put("time", time);
            System.out.println("Insert Failed " + values);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        database.insert("failed_certificate", null, values);
        database.close();
    }

    // Calculating Total Valid Certificates
    public int totalCertificates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM scanned_certificate " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Calculating Total Invalid Certificates
    public int totalInvalidCertificates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM failed_certificate " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Computing Reference Number of the First Scanned Certificate
    public JSONArray getFirstCertificate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM scanned_certificate WHERE date ='" + dateFormat.format(today) + "' ORDER BY time ASC LIMIT 1";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        JSONArray firstCertificate = new JSONArray();

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject _firstCertificate = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if(cursor.getColumnName(i) != null) {
                    try {
                        if(cursor.getString(i) != null) {
                            _firstCertificate.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            _firstCertificate.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.e("Error", Objects.requireNonNull(e.getMessage()));
                    }
                }
            }
            firstCertificate.put(_firstCertificate);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("First Certificate", String.valueOf(firstCertificate));
        return firstCertificate;
    }

    // Computing Reference Number of the Last Scanned Certificate
    public JSONArray getLastCertificate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM scanned_certificate WHERE date ='" + dateFormat.format(today) + "' ORDER BY time DESC LIMIT 1";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        JSONArray lastCertificate = new JSONArray();

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject _lastCertificate = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if(cursor.getColumnName(i) != null) {
                    try {
                        if(cursor.getString(i) != null) {
                            _lastCertificate.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            _lastCertificate.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.e("Error", Objects.requireNonNull(e.getMessage()));
                    }
                }
            }
            lastCertificate.put(_lastCertificate);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("Last Certificate", String.valueOf(lastCertificate));
        return lastCertificate;
    }
}
