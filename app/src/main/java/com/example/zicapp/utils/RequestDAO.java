package com.example.zicapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// The class to store offline requests to be sync online later
public class RequestDAO {
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;
    private SQLiteDatabase departureDatabase;

    public RequestDAO(Context context) { dbHelper = new DatabaseHelper(context); }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // A function to add arrival certificate offline in order to sync later
    public void addCertificateRequest(String authToken, String referenceNumber){
        try{
            database.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_AUTH_TOKEN, authToken);
            values.put(DatabaseHelper.COLUMN_REFERENCE_NUMBER, referenceNumber);
            database.insert(DatabaseHelper.CERTIFICATES_TABLE, null, values);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            database.endTransaction();
            database.close();
        }
    }

    // A function to add departure certificate offline in order to sync later
    public void addDepartureRequest(String authToken, String referenceNumber){
        try{
            database.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.DEPARTURE_COLUMN_AUTH_TOKEN, authToken);
            values.put(DatabaseHelper.DEPARTURE_COLUMN_REFERENCE_NUMBER, referenceNumber);
            database.insert(DatabaseHelper.DEPARTURE_TABLE, null, values);
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    // List of offline arrival certificate scanned in form of array
    public List<OfflineCertificatesRequest> getArrivalRequests(){
        List<OfflineCertificatesRequest> requests = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.CERTIFICATES_TABLE,
                null, null, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String authToken = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTH_TOKEN));
                String referenceNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REFERENCE_NUMBER));
                OfflineCertificatesRequest request = new OfflineCertificatesRequest(authToken, referenceNumber);
                request.setId(id);
                requests.add(request);
            } while(cursor.moveToNext());
            cursor.close();
        }


        return requests;
    }

    // List of offline departure certificate scanned in form of array
    public List<OfflineDepartureRequest> getDepartureRequests(){
        List<OfflineDepartureRequest> requests = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.DEPARTURE_TABLE,
                null, null, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.DEPARTURE_COLUMN_ID));
                String authToken = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.DEPARTURE_COLUMN_AUTH_TOKEN));
                String referenceNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.DEPARTURE_COLUMN_REFERENCE_NUMBER));
                OfflineDepartureRequest request = new OfflineDepartureRequest(authToken, referenceNumber);
                request.setId(id);
                requests.add(request);
            } while(cursor.moveToNext());
            cursor.close();
        }

        return requests;
    }

    // Function to delete offline arrival certificate after sync
    public void deleteArrivalRequest(long id) {
        database.delete(DatabaseHelper.CERTIFICATES_TABLE, DatabaseHelper.COLUMN_ID + "= ?", new String[]{String.valueOf(id)});
    }

    // Function to delete offline departure certificate after sync
    public void deleteDepartureRequest(long id) {
        database.delete(DatabaseHelper.DEPARTURE_TABLE, DatabaseHelper.DEPARTURE_COLUMN_ID + "= ?", new String[]{String.valueOf(id)});
    }
}
