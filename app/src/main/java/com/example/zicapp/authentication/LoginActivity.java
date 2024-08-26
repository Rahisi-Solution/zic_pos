package com.example.zicapp.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.zicapp.HomeActivity;
import com.example.zicapp.R;
import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(LoginActivity.this);
    View parentLayout;
    private ProgressDialog loginProgress;
    TextInputEditText officerNumber;
    TextInputEditText officerPIN;

    boolean logged_in = false;

    private final LoginActivity context = LoginActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton loginButton = findViewById(R.id.login_button);
        MaterialButton resetButton = findViewById(R.id.reset_button);
        officerNumber = findViewById(R.id.officer_number_field);
        officerPIN = findViewById(R.id.officer_pin_field);
        parentLayout = findViewById(android.R.id.content);

        loginButton.setOnClickListener(view -> {
            if(String.valueOf(officerNumber.getText()).trim().isEmpty()) {
                showSnackBar("Please Enter Officer Number");
                Log.e(Config.LOG_TAG, "Enter number");
            } else if (String.valueOf(officerPIN.getText()).trim().isEmpty()) {
                showSnackBar("Please Enter Officer PIN");
                Log.e(Config.LOG_TAG, "Enter pin");
            } else {
                if(isOnline(this)){
                    loginRequest(String.valueOf(officerNumber.getText()), String.valueOf(officerPIN.getText()));
                } else {
                    showSnackBar("You are offline, please connect to internet to continue");
                }

            }
        });

        officerPIN.setOnKeyListener((view, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                hideOnScreenKeyboard();
                if(String.valueOf(officerNumber.getText()).trim().isEmpty()) {
                    showSnackBar("Please Enter Officer Number");
                    Log.e(Config.LOG_TAG, "Enter number");
                } else if (String.valueOf(officerPIN.getText()).trim().isEmpty()) {
                    showSnackBar("Please Enter Officer PIN");
                    Log.e(Config.LOG_TAG, "Enter pin");
                } else {
                    if(isOnline(this)){
                        loginRequest(String.valueOf(officerNumber.getText()), String.valueOf(officerPIN.getText()));
                    } else {
                        showSnackBar("You are offline, please connect to internet to continue");
                    }

                }
            }
            return false;
        });

        resetButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    // Check Device internet connectivity
    public static boolean isOnline(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        if(connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        } else {
            return false;
        }
    }

    private void hideOnScreenKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(officerNumber.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void loginRequest(String officer_number, String officer_pin) {
        Log.e(Config.LOG_TAG, officer_number);
        Log.e(Config.LOG_TAG, officer_pin);

        loginProgress = ProgressDialog.show(LoginActivity.this, "Processing", "Please wait...");

        StringRequest request = new StringRequest(Request.Method.POST, Config.OFFICER_LOGIN,
                response -> {
                    loginProgress.dismiss();
                    Log.e("Login Response: ", response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject officerResponse = jsonObject.getJSONObject("response");
                        String code = officerResponse.getString("code");
                        String message = officerResponse.getString("message");

                        if(code.equals("200")){
                            JSONObject userDetails = jsonObject.getJSONObject("user_details");
                            String inspectorType = userDetails.getString("inspector_type");

                            if(inspectorType.equals("2")) {
                                String auth_token = userDetails.getString("token");
                                String login_credential = userDetails.getString("login_credential_id");
                                String user_id = userDetails.getString("user_id");
                                String user_name = userDetails.getString("username");
                                String domain = userDetails.getString("domain");
                                String entrypoint_id = userDetails.getString("entrypoint_id");
                                String entrypoint = userDetails.getString("entrypoint");
                                JSONArray applications = userDetails.getJSONArray("applications");

                                Log.e("APPLICATIONS", String.valueOf(applications));

                                saveApplications(applications);

                                SharedPreferences preferences = LoginActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putString(Config.AUTH_TOKEN, auth_token);
                                editor.putString(Config.LOGIN_CREDENTIAL, login_credential);
                                editor.putString(Config.USER_ID, user_id);
                                editor.putString(Config.USER_NAME, user_name);
                                editor.putString(Config.DOMAIN, domain);
                                editor.putString(Config.ENTRYPOINT_ID, entrypoint_id);
                                editor.putString(Config.ENTRYPOINT, entrypoint);
                                editor.putBoolean(Config.LOGGED_IN_PREF, true);
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                showSnackBar("Error: Officer does not registered under this Institution" + "");
                            }
                        } else {
                            showSnackBar("Something happened " + message);
                        }

                    } catch (JSONException exception) {
                        showSnackBar("Exception: " + exception);
                    }
                },
                error -> {
                    loginProgress.dismiss();
                    try {
                        if(String.valueOf(error).equals("com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"earrival.rahisi.co.tz\": No address associated with hostname")){
                            System.out.println("The error HERE = " + error);
                            showSnackBar("Network Error please check your Internet Bandwith");
                        } else {
                            showSnackBar(String.valueOf(error));
                        }
                    } catch (Exception e) {
                        showSnackBar("unknown error: " + e);
                    }
                }){
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", officer_number);
                params.put("password", officer_pin);
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

    private void saveApplications(JSONArray applications) {
        if (applications.length() > 0) {
            for (int i = 0; i < applications.length(); i++) {
                try {
                    JSONObject applicationObject = applications.getJSONObject(i);
                    String name = applicationObject.getString("name");
                    String referenceNumber = applicationObject.getString("reference_number");
                    String nationality = applicationObject.getString("nationality");
                    String arrivalDate = applicationObject.getString("arrival_date");
                    String passportNumber = applicationObject.getString("passport_number");
                    String birthDate = applicationObject.getString("birth_date");
                    String applicationStatus = applicationObject.getString("insuarance_status");
//                    String insuranceStatus = applicationObject.getString("application_status");

                    JSONObject _applicationObject = new JSONObject();
                    _applicationObject.put("name", name);
                    _applicationObject.put("reference_number", referenceNumber);
                    _applicationObject.put("nationality", nationality);
                    _applicationObject.put("arrival_date",  arrivalDate);
                    _applicationObject.put("birth_date",  birthDate);
                    _applicationObject.put("passport_number", passportNumber);
                    _applicationObject.put("application_status", applicationStatus);

                    Log.e("Inserted Applications", String.valueOf(_applicationObject));
                    offlineDB.insertApplications(_applicationObject);

                } catch (JSONException exception) {
                    Log.e("APPLICATIONS LOOP", String.valueOf(exception));
                }
            }
        }
    }

    void showSnackBar(String displayMessage) {
        Snackbar snackbar;
        snackbar = Snackbar.make(parentLayout, displayMessage, Snackbar.LENGTH_SHORT);
        View snack_view = snackbar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)snack_view.getLayoutParams();
        params.gravity = Gravity.TOP;
        snack_view.setBackgroundColor(0xFFe56b6f);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        logged_in = preferences.getBoolean(Config.LOGGED_IN_PREF, false);

        if(logged_in) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            System.out.println("Login False");
//            showSnackBar("Logged in status: FALSE");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        manager.moveTaskToFront(getTaskId(),0);
    }
}