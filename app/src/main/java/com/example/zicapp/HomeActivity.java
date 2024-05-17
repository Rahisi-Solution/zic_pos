package com.example.zicapp;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(HomeActivity.this);
    SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    Date date = new Date();

    private String username;
    private String entrypoint;
    private String authToken;
    int total_certificates;

    View parentLayout;
    TextView user_name;
    TextView entry_point;
    TextView today_date;
    TextView total_visitors;

    private Dialog checkedOutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        prepareData();
        parentLayout = findViewById(android.R.id.content);
        total_certificates=offlineDB.totalCertificates();

        user_name = findViewById(R.id.officer_name);
        entry_point = findViewById(R.id.entry_point);
        today_date = findViewById(R.id.today_date);
        total_visitors = findViewById(R.id.total_checkin_count);

        MaterialCardView certificate = findViewById(R.id.certificate_card);
        MaterialCardView reports = findViewById(R.id.report_card);
        MaterialCardView settings = findViewById(R.id.settings_card);

        user_name.setText(username);
        entry_point.setText(entrypoint);
        today_date.setText(dateFormatter2.format(date));
        total_visitors.setText(String.valueOf(total_certificates));

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        reports.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Start Certificate Scanning
        certificate.setOnClickListener(view -> {
            if (checkPackage("com.telpo.tps550.api")) {
                Intent intent = new Intent();
                intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                scanCertificateProcedure.launch(intent);
            } else {
                showSnackBar("Scan valid QR to proceed");
            }
        });
    }

    // Prepare Data for Home Page Display
    private void prepareData() {
        SharedPreferences preferences = HomeActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        authToken = preferences.getString(Config.AUTH_TOKEN, "n.a");
        username = preferences.getString(Config.USER_NAME, "n.a");
        entrypoint = preferences.getString(Config.ENTRYPOINT, "n.a");

        System.out.println("User Logged In = " + username);


    }

    // Check the Package for Launching QR Code
    private boolean checkPackage(String packageName) {
        PackageManager manager = this.getPackageManager();
        Intent intent = new Intent().setPackage(packageName);
        List<ResolveInfo> info = manager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        if (info == null || info.size() < 1) {
            return false;
        } else {
            return true;
        }
    }

    // Start Scan Certificate Procedure
    ActivityResultLauncher<Intent> scanCertificateProcedure = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showSnackBar("Invalid QR Scanned");
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    if(result.getData() != null) {
                        String data = result.getData().getStringExtra("qrCode");
                        System.out.println("Qr Code data = " + data);
                        assert data != null;
                        if(data.startsWith("https://")){
                            showSuccessDialog();
                        } else {
                            showErrorDialog();
                        }
                    }
                }
            }
    );

    // Snack bar for display Error Messages
    void showSnackBar(String displayMessage) {
        Snackbar snackbar;
        snackbar = Snackbar.make(parentLayout, displayMessage, Snackbar.LENGTH_SHORT);
        View snackView = snackbar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackView.setBackgroundColor(0xFFe56b6f);
        snackbar.show();
    }

    // Success dialog to show when scan certificate success //
    private void showSuccessDialog() {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.success_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText("Valid Certificate");
        applicant_name.setText("Verification Successfully");
        description.setText("Welcome to Zanzibar");

        String scannedDate = dateFormatter.format(date);
        String scannedTime = timeFormatter.format(date);
        String referenceNumber = "CT-1200" + scannedTime;
        offlineDB.insertCertificate(referenceNumber, scannedDate, scannedTime);
        dismissButton.setOnClickListener(view -> {
            checkedOutDialog.dismiss();
            showSnackBar("👊👊 Welcome to Zanzibar");
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        checkedOutDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        checkedOutDialog.show();
    }

    // Error dialog to show when scan certificate failed //
    private void showErrorDialog() {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.error_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText("Invalid Certificate");
        applicant_name.setText("Verification Failed");
        description.setText("Please Apply  for valid ZIC Insurance");

        String scannedDate = dateFormatter.format(date);
        String scannedTime = timeFormatter.format(date);
        String referenceNumber = "CT-1300" + scannedTime;
        offlineDB.insertInvalidCertificate(referenceNumber, scannedDate, scannedTime);
        dismissButton.setOnClickListener(view -> {
            checkedOutDialog.dismiss();
            showSnackBar("Invalid Certificate");
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        checkedOutDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        checkedOutDialog.show();
    }
}