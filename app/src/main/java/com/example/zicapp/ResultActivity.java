package com.example.zicapp;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;
import com.example.zicapp.utils.RequestDAO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(ResultActivity.this);
    MaterialButton verify_button;
    TextView reference_number;
    TextView passport_number;
    TextView applicant_name;
    TextView nationality;
    TextView arrival_date;

    private String userName;
    private String authToken;
    private String referenceNumber;
    private String passportNumber;
    private String applicantName;
    private String nationalities;
    private String arrivalDate;

    private ProgressDialog searchDialog;
    View parentLayout;
    private Dialog checkedOutDialog;

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    Date date = new Date();
    private RequestDAO requestDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        verify_button = findViewById(R.id.verify_button_id);
        reference_number = findViewById(R.id.reference_number);
        passport_number = findViewById(R.id.passport_number);
        applicant_name = findViewById(R.id.applicant_name);
        nationality = findViewById(R.id.nationality);
        arrival_date = findViewById(R.id.arrival_date);
        parentLayout = findViewById(android.R.id.content);

        prepareData();

        requestDAO = new RequestDAO(this);
        requestDAO.open();

        verify_button.setOnClickListener(v -> {
            if(isOnline(this)){
                markVerified();
            } else {
                saveCertificateOfflineRequest(authToken, referenceNumber);
            }
        });
    }

    private void prepareData(){
        SharedPreferences preferences = ResultActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Bundle bundle = null;
        bundle = this.getIntent().getExtras();
        userName = preferences.getString(Config.USER_NAME, "n.a");
        authToken = preferences.getString(Config.AUTH_TOKEN, "n.a");
        assert bundle != null;
        referenceNumber = bundle.getString("reference_number");
        passportNumber = bundle.getString("passport_number");
        applicantName = bundle.getString("applicant_name");
        nationalities = bundle.getString("nationality");
        arrivalDate = bundle.getString("arrival_date");
        reference_number.setText(referenceNumber);
        passport_number.setText(passportNumber);
        applicant_name.setText(applicantName);
        nationality.setText(nationalities);
        arrival_date.setText(arrivalDate);
    }

    private void markVerified(){
        searchDialog = ProgressDialog.show(ResultActivity.this, "Processing", "Please wait...");

        StringRequest request = new StringRequest(Request.Method.POST, Config.MARK_IN_USE,
                response -> {
                    searchDialog.dismiss();
                    Log.e(Config.LOG_TAG, String.valueOf(response));
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject applicantResponse = jsonObject.getJSONObject("response");
                        JSONObject applicantData = jsonObject.getJSONObject("data");
                        String code = applicantResponse.getString("code");
                        String message = applicantResponse.getString("message");
                        String checkinReference = applicantData.getString("checkin_reference");

                        if(code.equals("200")) {
                            showSuccessDialog(checkinReference);
                        } else {
                            showSnackBar("Failed to checkin: " + message);
                        }

                    } catch (JSONException exception) {
                        showSnackBar("Request Error: " + exception);
                    }
                },
                error -> {
                   searchDialog.dismiss();
                    if(String.valueOf(error).equals("com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"earrival.rahisi.co.tz\": No address associated with hostname")){
                        System.out.println("The error HERE = " + error);
                        showSnackBar("Network Error please check your Internet Bandwith");
                    } else {
                        showSnackBar(String.valueOf(error));
                    }
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("authorization", "Bearer " + authToken);
                params.put("reference_number", referenceNumber);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(4000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void saveCertificateOfflineRequest(String authToken, String referenceNumber) {
        showSuccessDialog(referenceNumber);
        System.out.println("Data going offline: " + referenceNumber + " " + authToken);
        requestDAO.addCertificateRequest(authToken, referenceNumber);
    }

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

    private void showSuccessDialog(String checkinReference) {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.success_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText("Valid Certificate");
        applicant_name.setText("Verification Successfully");
        description.setText("Reference " + checkinReference);

        String scannedDate = dateFormatter.format(date);
        String scannedTime = timeFormatter.format(date);
        offlineDB.insertCertificate(referenceNumber, scannedDate, scannedTime);
        dismissButton.setOnClickListener(view -> {
            checkedOutDialog.dismiss();
            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        checkedOutDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        checkedOutDialog.show();
    }

    // Check Device internet connectivity
    public static boolean isOnline(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}