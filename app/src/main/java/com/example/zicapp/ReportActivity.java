package com.example.zicapp;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
    TextView firstCertificate;
    TextView lastCertificate;
    String today_date_report;

    int valid_certificate;
    int invalid_certificate;
    int total_certificates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        validCertificate = findViewById(R.id.valid_certificates_value);
        inValidCertificate = findViewById(R.id.invalid_certificates_value);
        totalCertificates = findViewById(R.id.total_certificates_count);
        firstCertificate = findViewById(R.id.first_certificate_value);
        lastCertificate = findViewById(R.id.last_certificate_value);

        valid_certificate = offlineDB.totalCertificates();
        invalid_certificate = offlineDB.totalInvalidCertificates();
        total_certificates = valid_certificate + invalid_certificate;

        validCertificate.setText(String.valueOf(valid_certificate));
        inValidCertificate.setText(String.valueOf(invalid_certificate));
        totalCertificates.setText(String.valueOf(total_certificates));

        getFirstCertificate();
        getLastCertificate();

        showTodayDate();
        today_date_report = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    // Get first Certificate Reference Number
    public void getFirstCertificate() {
        JSONArray firstCheckin = offlineDB.getFirstCertificate();
        for (int i = 0; i < firstCheckin.length(); i++) {
            try {
                JSONObject _firstCheckin = firstCheckin.getJSONObject(i);
                String first_certificate_ref = _firstCheckin.getString("reference_number");
                firstCertificate.setText(first_certificate_ref);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    // Get last Certificate Reference Number
    public void getLastCertificate() {
        JSONArray lastCheckin = offlineDB.getLastCertificate();

        for (int i = 0; i < lastCheckin.length(); i++) {
            try {
                JSONObject _lastCertificate = lastCheckin.getJSONObject(i);
                String last_certificate_ref = _lastCertificate.getString("reference_number");
                lastCertificate.setText(last_certificate_ref);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
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