package com.example.zicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.example.zicapp.authentication.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();

        // Start delay timer before go to login page
        int SPLASH_DISPLAY_LENGTH = 3000;
        handler.postDelayed(() -> {
            Intent loginActivity = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}