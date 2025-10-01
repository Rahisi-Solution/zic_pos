package com.example.zicapp;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.example.zicapp.HomeActivity.isOnline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.common.apiutil.decode.DecodeReader;
import com.example.zicapp.authentication.ChangePassword;
import com.example.zicapp.authentication.LoginActivity;
import com.example.zicapp.claimInspector.ClaimInspectorActivity;
import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineCertificatesRequest;
import com.example.zicapp.utils.OfflineDB;
import com.example.zicapp.utils.OfflineDepartureRequest;
import com.example.zicapp.utils.RequestDAO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    MaterialButton logoutButton;
    Spinner spinner;
    View parentLayout;
    private  String entryPoint;
    private  String inspectorType;
    TextView change_password;
    private RequestDAO requestDAO;
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    Date date = new Date();
    public static final String[] languages = {"Select Language", "English", "Swahili"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        prepareData();
        parentLayout = findViewById(android.R.id.content);

        requestDAO = new RequestDAO(this);
        requestDAO.open();

        syncArrivalCertificatesOfflineRequest();
        syncDepartureCertificatesOfflineRequest();

        logoutButton = findViewById(R.id.logout_button);
        TextView today_date = findViewById(R.id.today_date);
        spinner = findViewById(R.id.spinner_sample);
        TextView entry_point = findViewById(R.id.entry_point);
        change_password = findViewById(R.id.change_password);

        entry_point.setText(entryPoint);

        today_date.setText(formatter.format(date));

        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                languages);
        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                String selectedLang = parent.getItemAtPosition(position).toString();
                if (selectedLang.equals("English")){
                    setLocal(SettingsActivity.this, "en");
                    finish();
                    startActivity(intent);
                } else if (selectedLang.equals("Swahili")) {
                    setLocal(SettingsActivity.this, "sw");
                    finish();
                    startActivity(intent);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        logoutButton.setOnClickListener(view -> {
            _showSignOutDialog();
        });
    }

    // Prepare Data for Home Page Display
    private void prepareData() {
        SharedPreferences preferences = SettingsActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
         entryPoint = preferences.getString(Config.ENTRYPOINT, "n.a");
         inspectorType = preferences.getString(Config.INSPECTOR_TYPE, "n.a");
    }

    // Method to sync the offline request to online
    private void syncArrivalCertificatesOfflineRequest() {
        if(isOnline(this)){
            List<OfflineCertificatesRequest> requests = requestDAO.getArrivalRequests();
            for(OfflineCertificatesRequest request : requests ) {
                sendArrivalCertificatesOfflineRequest(request);
            }
        }
    }

    // Method to sync the offline request to online
    private void syncDepartureCertificatesOfflineRequest() {
        if(isOnline(this)){
            List<OfflineDepartureRequest> requests = requestDAO.getDepartureRequests();
            for(OfflineDepartureRequest request : requests ) {
                departureSendCertificatesOfflineRequest(request);
            }
        }
    }

    // Request to send arrival data to API when device comes ONLINE
    private void sendArrivalCertificatesOfflineRequest(OfflineCertificatesRequest request) {
        StringRequest certificateRequest = new StringRequest(Request.Method.POST, Config.MARK_IN_USE,
                response -> {
                    System.out.println("OFFLINE ARRIVAL CERTIFICATE RESPONSE:  " + response);
                    try{
                        JSONObject jsonObject = new JSONObject(String.valueOf(response));
                        JSONObject certificateResponse = jsonObject.getJSONObject("response");
                        String code = certificateResponse.getString("code");
                        String message =certificateResponse.getString("message");

                        if(code.equals("200")){
                            JSONObject certificateDetails = jsonObject.getJSONObject("data");
                            System.out.println("CERTIFICATE OFFLINE DETAILS: " + certificateDetails);
                            requestDAO.deleteArrivalRequest(request.getId());
                            System.out.println("DATA SENT OFFLINE SUCCESSFULLY");
                        }else {
                            Log.e("", message);
                        }
                    }catch(JSONException e){
                        Log.e("Certificate response Error ", String.valueOf(e));
                        showSnackBar("Certificate response Error");
                    }
                },
                error -> {
                    Log.e("", String.valueOf(error));
                    showSnackBar("Check network to sync data");
                }
        ){
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("authorization", "Bearer " + request.getAuthToken());
                params.put("reference_number", request.getReferenceNumber());
                System.out.println("Parameters to put from offline " + params);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        certificateRequest.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(certificateRequest);
    }

    // Request to send departure data to API when device comes ONLINE
    private void departureSendCertificatesOfflineRequest(OfflineDepartureRequest request) {
        StringRequest certificateRequest = new StringRequest(Request.Method.POST, Config.MARK_SEIZED,
                response -> {
                    System.out.println("Departure offline Request = " + request);
                    System.out.println("OFFLINE DEPARTURE SYNC CERTIFICATE RESPONSE :" + response);
                    try{
                        JSONObject jsonObject = new JSONObject(String.valueOf(response));
                        JSONObject certificateResponse = jsonObject.getJSONObject("response");
                        String code = certificateResponse.getString("code");
                        String message =certificateResponse.getString("message");

                        if(code.equals("200")){
                            JSONObject certificateDetails = jsonObject.getJSONObject("data");
                            System.out.println("DEPARTURE CERTIFICATE OFFLINE DETAILS :" + certificateDetails);
                            requestDAO.deleteDepartureRequest(request.getId());
                            System.out.println("DEPARTURE DATA SENT OFFLINE SUCCESSFULLY");
                        }else {

                            Log.e("", message);
                        }
                    }catch(JSONException e){
                        Log.e("Certificate response Error ", String.valueOf(e));
                        showSnackBar("Certificate response Error" + e);
                    }
                },
                error -> {
                    Log.e("", String.valueOf(error));
                    showSnackBar("Certificate response Error");
                }
        ){
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("authorization", "Bearer " + request.getAuthToken());
                params.put("reference_number", request.getReferenceNumber());
                System.out.println("Parameters to put from offline " + params);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        certificateRequest.setRetryPolicy(new DefaultRetryPolicy (40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(certificateRequest);
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

    private void _showSignOutDialog() {
        AlertDialog.Builder _dialog = new AlertDialog.Builder(this);
        _dialog.setTitle("Are You Sure?");
        _dialog.setMessage("Do you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    OfflineDB offlineDB = new OfflineDB(this);
                    offlineDB.clearCertificates();
                    offlineDB.close();

                    SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    preferences.edit().clear().apply();

                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null);
        AlertDialog signOut = _dialog.create();
        signOut.show();
    }

    public void setLocal(Activity activity, String langCode){
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if(Objects.equals(inspectorType, "3")){
            intent = new Intent(SettingsActivity.this, ClaimInspectorActivity.class);
        }else {
            intent = new Intent(SettingsActivity.this, HomeActivity.class);
        }
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    public void onChangePassword(View view) {
        Intent intent = new Intent(SettingsActivity.this, ChangePassword.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}