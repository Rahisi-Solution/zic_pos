package com.example.zicapp;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(ReportActivity.this);

    TextView validCertificate;
    TextView inValidCertificate;
    TextView totalCertificates;
    TextView officerName;
    String today_date_report;
    String username;
    int valid_certificate;
    int invalid_certificate;
    int total_certificates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        officerName = findViewById(R.id.officer_name_report);
        validCertificate = findViewById(R.id.valid_arrival_value);
        inValidCertificate = findViewById(R.id.invalid_arrival_value);
        totalCertificates = findViewById(R.id.total_arrival_count);

        valid_certificate = offlineDB.totalCertificates();
        invalid_certificate = offlineDB.totalInvalidCertificates();
        total_certificates = valid_certificate + invalid_certificate;

        validCertificate.setText(String.valueOf(valid_certificate));
        inValidCertificate.setText(String.valueOf(invalid_certificate));
        totalCertificates.setText(String.valueOf(total_certificates));

        showTodayDate();
        today_date_report = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        officerName.setText(username);
        System.out.println(username);
        officerName.setTextColor(getResources().getColor(R.color.black));
        SharedPreferences preferences = ReportActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(Config.USER_NAME, "n.a");
    }

    // Call the method to show today's date
    public void showTodayDate() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        TextView dateReport = findViewById(R.id.today_date_report);
        dateReport.setText(todayDate);
    }

    // When back button key is presses return to Home Screen
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ReportActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}