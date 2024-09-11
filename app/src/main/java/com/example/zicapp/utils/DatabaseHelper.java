package com.example.zicapp.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CERTIFICATES_DATABASE = "offline_certificate_db";
    private static final int DATABASE_VERSION = 1;

    public static final String CERTIFICATES_TABLE = "certificates_table";
    public static final String DEPARTURE_TABLE = "departure_table";
    public static final String COLUMN_ID = "id";
    public static final String DEPARTURE_COLUMN_ID = "id";
    public static final String COLUMN_AUTH_TOKEN = "auth_token";
    public static final String DEPARTURE_COLUMN_AUTH_TOKEN = "auth_token";
    public static final String COLUMN_REFERENCE_NUMBER = "reference_number";
    public static final String DEPARTURE_COLUMN_REFERENCE_NUMBER = "reference_number";

    private static final String CERTIFICATES_CREATE =
            "CREATE TABLE " + CERTIFICATES_TABLE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_AUTH_TOKEN + " TEXT, " +
            COLUMN_REFERENCE_NUMBER + " TEXT);";

    private static final String DEPARTURE_CREATE =
            "CREATE TABLE " + DEPARTURE_TABLE + " (" +
            DEPARTURE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DEPARTURE_COLUMN_AUTH_TOKEN + " TEXT, " +
            DEPARTURE_COLUMN_REFERENCE_NUMBER + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, CERTIFICATES_DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CERTIFICATES_CREATE);
        db.execSQL(DEPARTURE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CERTIFICATES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DEPARTURE_TABLE);
        onCreate(db);
    }
}
