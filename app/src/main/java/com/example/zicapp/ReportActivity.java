package com.example.zicapp;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(ReportActivity.this);

    TextView validArrivalCertificate;
    TextView inValidArrivalCertificate;
    TextView totalArrivalCertificates;
    TextView validDepartureCertificate;
    TextView inValidDepartureCertificate;
    TextView totalDepartureCertificates;

    TextView user_name;
    TextView entry_point;
    String today_date_report;
    public String username;
    public String entryPoint;
    int valid_arrival_certificate;
    int invalid_arrival_certificate;
    int total_arrival_certificates;
    int valid_departure_certificate;
    int invalid_departure_certificate;
    int total_departure_certificates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        user_name = findViewById(R.id.officer_name);
        entry_point = findViewById(R.id.entry_point);
        validArrivalCertificate = findViewById(R.id.valid_arrival_value);
        inValidArrivalCertificate = findViewById(R.id.invalid_arrival_value);
        totalArrivalCertificates = findViewById(R.id.total_arrival_count);
        validDepartureCertificate = findViewById(R.id.valid_departure_value);
        inValidDepartureCertificate = findViewById(R.id.invalid_departure_value);
        totalDepartureCertificates = findViewById(R.id.total_departure_count);

        valid_arrival_certificate = offlineDB.totalArrivalCertificates();
        invalid_arrival_certificate = offlineDB.totalInvalidCertificates();
        total_arrival_certificates = valid_arrival_certificate + invalid_arrival_certificate;
        valid_departure_certificate = offlineDB.totalDepartureCertificates();
        invalid_departure_certificate = offlineDB.totalInvalidDeparture();
        total_departure_certificates = valid_departure_certificate + invalid_departure_certificate;

        validArrivalCertificate.setText(String.valueOf(valid_arrival_certificate));
        inValidArrivalCertificate.setText(String.valueOf(invalid_arrival_certificate));
        totalArrivalCertificates.setText(String.valueOf(total_arrival_certificates));
        validDepartureCertificate.setText(String.valueOf(valid_departure_certificate));
        inValidDepartureCertificate.setText(String.valueOf(invalid_departure_certificate));
        totalDepartureCertificates.setText(String.valueOf(total_departure_certificates));

        showTodayDate();
        today_date_report = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        user_name.setTextColor(getResources().getColor(R.color.primary_color));
        SharedPreferences preferences = ReportActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(Config.USER_NAME, "n.a");
        entryPoint = preferences.getString(Config.ENTRYPOINT, "n.a");
        user_name.setText(username);
        entry_point.setText(entryPoint);
        System.out.println(username);
        System.out.println("Entry point on report " + entryPoint);
    }

    // Call the method to show today's date
    public void showTodayDate() {
        String todayDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
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