package com.example.zicapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
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
    String departure_certificate;
    String failed_certificate;
    String failed_departure;
    String checked_in;
    String applications;

    // Creating Tables in offline database (SQLite)
    scanned_certificate =  "CREATE TABLE scanned_certificate (reference_number TEXT, date TEXT, time TEXT)";
    departure_certificate =  "CREATE TABLE departure_certificate (reference_number TEXT, date TEXT, time TEXT)";
    failed_certificate =  "CREATE TABLE failed_certificate (reference_number TEXT, date TEXT, time TEXT)";
    failed_departure =  "CREATE TABLE failed_departure (reference_number TEXT, date TEXT, time TEXT)";
    checked_in = "CREATE TABLE checked_in (reference_number TEXT, name TEXT, checkins TEXT, date TEXT)";
    applications = "CREATE TABLE applications (reference_number TEXT, name TEXT, nationality TEXT, arrival_date TEXT,birth_date TEXT, passport_number TEXT, application_status TEXT)";

    // Executing query to Create Table in offline database (SQLite)
    sqLiteDatabase.execSQL(scanned_certificate);
    sqLiteDatabase.execSQL(departure_certificate);
    sqLiteDatabase.execSQL(failed_certificate);
    sqLiteDatabase.execSQL(failed_departure);
    sqLiteDatabase.execSQL(checked_in);
    sqLiteDatabase.execSQL(applications);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        // Drop Table on the Database Upgrade
        String scannedCertificates = "DROP TABLE IF EXISTS scanned_certificate";
        String departureCertificates = "DROP TABLE IF EXISTS departure_certificate";
        String failedCertificate = "DROP TABLE IF EXISTS failed_certificate";
        String failedDeparture = "DROP TABLE IF EXISTS failed_certificate";
        String checked_in = "DROP TABLE IF EXISTS checked_in";
        String applications = "DROP TABLE IF EXISTS applications";

        // Executing Drop Table Queries
        sqLiteDatabase.execSQL(scannedCertificates);
        sqLiteDatabase.execSQL(departureCertificates);
        sqLiteDatabase.execSQL(failedCertificate);
        sqLiteDatabase.execSQL(failedDeparture);
        sqLiteDatabase.execSQL(checked_in);
        sqLiteDatabase.execSQL(applications);
    }

    public void insertApplications(JSONObject jsonObject) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", jsonObject.getString("reference_number"));
            values.put("name", jsonObject.getString("name"));
            values.put("nationality", jsonObject.getString("nationality"));
            values.put("arrival_date", jsonObject.getString("arrival_date"));
            values.put("birth_date", jsonObject.getString("birth_date"));
            values.put("passport_number", jsonObject.getString("passport_number"));
            values.put("application_status", jsonObject.getString("application_status"));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        database.insert("applications", null, values);
        database.close();

    }

    // Inserting Arrival Certificate in scanned_certificate Table
    public void insertArrivalCertificate(String reference_number, String date, String time){
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

    // Inserting Departure Certificate in scanned_certificate Table
    public void insertDepartureCertificate(String reference_number, String date, String time){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", reference_number);
            values.put("date", date);
            values.put("time", time);
            System.out.println("Insert departure Certificate " + values);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        database.insert("departure_certificate", null, values);
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

    // Inserting Invalid Certificate in failed_departure Table
    public void insertInvalidDeparture(String reference_number, String date, String time){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", reference_number);
            values.put("date", date);
            values.put("time", time);
            System.out.println("Insert Failed Departure" + values);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        database.insert("failed_departure", null, values);
        database.close();
    }

    // Calculating Total Valid Certificates
    public int totalArrivalCertificates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM scanned_certificate " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    // Calculating Total Invalid Certificates
    public int totalDepartureCertificates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM departure_certificate " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    // Calculating Total Invalid Arrival
    public int totalInvalidCertificates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM failed_certificate " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
        return count;
    }

    // Calculating Total Invalid Departure
    public int totalInvalidDeparture() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date today = new Date();
        String query = "SELECT * FROM failed_departure " + "WHERE TRIM(date)='" + dateFormat.format(today) +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        database.close();
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
        database.close();
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
        database.close();
        Log.d("Last Certificate", String.valueOf(lastCertificate));
        return lastCertificate;
    }

    // Computing All Application Certificate
    public JSONObject getApplication(String reference_number) {
        String query = "SELECT * FROM applications WHERE reference_number='" + reference_number +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        JSONObject applicationObject = new JSONObject();

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if(cursor.getString(i) != null) {
                            applicationObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            applicationObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception exception) {
                        Log.e("Error", Objects.requireNonNull(exception.getMessage()));
                    }
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        Log.e("Got Application", String.valueOf(applicationObject));
        return applicationObject;
    }

    public JSONObject getCertificate(String reference_number) {
        String query = "SELECT * FROM scanned_certificate WHERE reference_number='" + reference_number +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        JSONObject applicationObject = new JSONObject();

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if(cursor.getString(i) != null) {
                            applicationObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            applicationObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception exception) {
                        Log.e("Error", Objects.requireNonNull(exception.getMessage()));
                    }
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        Log.e("Got Certificate", String.valueOf(applicationObject));
        return applicationObject;
    }

    public JSONObject getDepartureCertificate(String reference_number) {
        String query = "SELECT * FROM departure_certificate WHERE reference_number='" + reference_number +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        JSONObject applicationObject = new JSONObject();

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if(cursor.getString(i) != null) {
                            applicationObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            applicationObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception exception) {
                        Log.e("Error", Objects.requireNonNull(exception.getMessage()));
                    }
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        Log.e("Got Departure Certificate", String.valueOf(applicationObject));
        return applicationObject;
    }

    // Inserting Insert Failed Certificate Table
    public void insertFailedCheckins(String reference_number, String applicant_name, String status, String date) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("reference_number", reference_number);
            values.put("applicantName", applicant_name);
            values.put("status", status);
            values.put("date", date);
            System.out.println("Insert failed checkin " + values);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // 13-05-2024 10:59:11 GMT+03:00

        database.insert("failed_checkins", null, values);
        database.close();
    }

    public void updateCertificateStatusToInUse(String certificateNumber, String status) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            String updateQuery = "UPDATE applications SET application_status = '" + status + "' WHERE TRIM(reference_number)='" + certificateNumber +"'";
            System.out.println("Update Application status👍: " + updateQuery);
            Log.e("Update Application status", updateQuery);
            database.execSQL(updateQuery);
        } catch (SQLException e) {
            System.out.println("Update Status Error👎: " + e.getMessage());
            Log.e("Update Status Error", e.getMessage());
        }
        database.close();
    }

    public void updateCertificateStatusToSeized(String certificateNumber, String status) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            String updateQuery = "UPDATE applications SET application_status = '" + status + "' WHERE TRIM(reference_number)='" + certificateNumber +"'";
            Log.i("Update Application status", updateQuery);
            database.execSQL(updateQuery);
        } catch (SQLException e) {
            Log.e("Update Tonnage Error", e.getMessage());
        }
        database.close();
    }

    public void clearCertificates() {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.execSQL("DELETE FROM scanned_certificate");
            database.execSQL("DELETE FROM departure_certificate");
            database.execSQL("DELETE FROM failed_certificate");
            database.execSQL("DELETE FROM failed_departure");
            Log.i("OfflineDB", "All certificate records cleared successfully.");
        } catch (SQLException e) {
            Log.e("OfflineDB", "Error while clearing certificates: " + e.getMessage());
        } finally {
            database.close();
        }
    }

}
