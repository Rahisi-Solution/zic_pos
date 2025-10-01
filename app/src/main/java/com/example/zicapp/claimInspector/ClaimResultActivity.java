package com.example.zicapp.claimInspector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zicapp.HomeActivity;
import com.example.zicapp.R;
import com.example.zicapp.ResultActivity;
import com.example.zicapp.utils.Config;
import com.google.android.material.button.MaterialButton;

public class ClaimResultActivity extends AppCompatActivity {

    TextView reference_number;
    TextView passport_number;
    TextView applicant_name;
    TextView nationality;
    TextView birth_date;
    TextView insurance_status;
    MaterialButton cancelButton;
    MaterialButton nextButton;

    private String referenceNumber;
    private String passportNumber;
    private String applicantName;
    private String nationalities;
    private String birthDate;
    private String insuranceStatus;
    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_result);

        reference_number = findViewById(R.id.reference_number);
        passport_number = findViewById(R.id.passport_number);
        applicant_name = findViewById(R.id.applicant_name);
        nationality = findViewById(R.id.nationality);
        birth_date = findViewById(R.id.birth_date);
        insurance_status = findViewById(R.id.insurance_status);
        cancelButton = findViewById(R.id.cancel_btn);
        nextButton = findViewById(R.id.next_btn);

        prepareData();

        cancelButton.setOnClickListener(v -> {
            Intent i = new Intent(ClaimResultActivity.this, ClaimInspectorActivity.class);
            startActivity(i);
            finish();
        });

        nextButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Config.INCOMING_TAG, tag);
            bundle.putString("reference_number", referenceNumber);
            Intent i = new Intent(ClaimResultActivity.this, ClaimFormActivity.class);
            i.putExtras(bundle);
            startActivity(i);
        });
    }

    private void prepareData(){
        SharedPreferences preferences = ClaimResultActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Bundle bundle = null;
        bundle = this.getIntent().getExtras();
        assert bundle != null;
        referenceNumber = bundle.getString("reference_number");
        passportNumber = bundle.getString("passport_number");
        applicantName = bundle.getString("applicant_name");
        nationalities = bundle.getString("nationality");
        birthDate = bundle.getString("birth_date");
        insuranceStatus = bundle.getString("application_status");
        reference_number.setText(referenceNumber);
        passport_number.setText(passportNumber);
        applicant_name.setText(applicantName);
        nationality.setText(nationalities);
        birth_date.setText(birthDate);
        insurance_status.setText(insuranceStatus);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ClaimResultActivity.this, ClaimInspectorActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
