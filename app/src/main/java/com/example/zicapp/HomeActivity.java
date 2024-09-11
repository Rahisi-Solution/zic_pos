package com.example.zicapp;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineCertificatesRequest;
import com.example.zicapp.utils.OfflineDB;
import com.example.zicapp.utils.OfflineDepartureRequest;
import com.example.zicapp.utils.RequestDAO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(HomeActivity.this);
    SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    Date date = new Date();
    public String username;
    private String entrypoint;
    int total_arrival;
    int total_departure;
    View parentLayout;
    TextView user_name;
    TextView entry_point;
    TextView today_date;
    TextView total_checkedin;
    TextView total_checkedout;
    private String authToken;
    String applicantName;
    String referenceNumber;
    String nationality;
    String arrivalDate;
    String birthDate;
    String passportNumber;
    String applicationStatus;
    private Dialog checkedOutDialog;
    private ProgressDialog searchDialog;
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private String tag;
    private RequestDAO requestDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        requestDAO = new RequestDAO(this);
        requestDAO.open();

        syncCertificatesOfflineRequest();
        syncDepartureCertificatesOfflineRequest();

        prepareData();
        parentLayout = findViewById(android.R.id.content);
        total_arrival = offlineDB.totalArrivalCertificates();
        total_departure = offlineDB.totalDepartureCertificates();

        user_name = findViewById(R.id.officer_name);
        entry_point = findViewById(R.id.entry_point);
        today_date = findViewById(R.id.today_date);
        total_checkedin = findViewById(R.id.total_checkedin_count);
        total_checkedout = findViewById(R.id.total_checkedout_count);

        MaterialCardView arrivalCertificateScan = findViewById(R.id.certificate_card);
        MaterialCardView departureCertificateScan = findViewById(R.id.departure_certificate_card);
        MaterialCardView reports = findViewById(R.id.report_card);
        MaterialCardView settings = findViewById(R.id.settings_card);

        user_name.setText(username);
        System.out.println(username);
        entry_point.setText(entrypoint);
        today_date.setText(dateFormatter2.format(date));
        total_checkedin.setText(String.valueOf(total_arrival));
        total_checkedout.setText(String.valueOf(total_departure));

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Navigate to report page to see daily report 
        reports.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Start Arrival Certificate Scanning
        arrivalCertificateScan.setOnClickListener(view -> {
            if (checkPackage("com.telpo.tps550.api")) {
                Intent intent = new Intent();
                intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                arrivalScanCertificateProcedure.launch(intent);
            } else {
                showSnackBar("Scan valid QR to proceed");
            }
        });

        // Start Departure Certificate Scanning
        departureCertificateScan.setOnClickListener(view -> {
            if (checkPackage("com.telpo.tps550.api")) {
                Intent intent = new Intent();
                intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                departureScanCertificateProcedure.launch(intent);
            } else {
                showSnackBar("Scan valid QR to proceed");
            }
        });
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

    // Prepare Data for Home Page Display
    private void prepareData() {
        SharedPreferences preferences = HomeActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(Config.USER_NAME, "n.a");
        entrypoint = preferences.getString(Config.ENTRYPOINT, "n.a");
        authToken = preferences.getString(Config.AUTH_TOKEN, "n.a");
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

    // Start Scan Certificate Procedure on arrival
    ActivityResultLauncher<Intent> arrivalScanCertificateProcedure = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showSnackBar("Invalid QR Scanned");
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    if(result.getData() != null) {
                        String data = result.getData().getStringExtra("qrCode");
                        System.out.println("Qr Code data = " + data);
                        assert data != null;
                        String passedData = Config.removeDoubleQuotes(data);
                        if(passedData.startsWith("IA") || passedData.startsWith("ZIC")) {
                            JSONObject applicationData = offlineDB.getCertificate(Config.removeDoubleQuotes(data));
                            System.out.println("Offline certificate " + applicationData);
                            if(isOnline(this)){
                                onlineArrivalSearchCertificateReference(data);
                            } else {
                                offlineArrivalSearchCertificateReference(data);
                            }
                        }
                        }else {
                            showSnackBar("Invalid Qr Code");
                        }
                    }
            }
    );

    // Start Scan Certificate Procedure on departure
    ActivityResultLauncher<Intent> departureScanCertificateProcedure = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    showSnackBar("Invalid QR Scanned");
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    if(result.getData() != null) {
                        String data = result.getData().getStringExtra("qrCode");
                        System.out.println("Qr Code data = " + data);
                        assert data != null;
                        String passedData = Config.removeDoubleQuotes(data);
                        if(passedData.startsWith("IA") || passedData.startsWith("ZIC")){
                            if(isOnline(this)){
                                System.out.println("Departure Online");
                                onlineDepartureSearchCertificateReference(data);
                            } else {
                                offlineDepartureSearchCertificateReference(data);
                            }
                        } else {
                            showSnackBar("Invalid Qr Code");
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


    /* Error dialog to show when scan certificate failed */
    private void showErrorDialog(String sms) {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.error_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText(sms);
        applicant_name.setText(R.string.error);
        description.setText(R.string.verification_failed);

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

    /* Error dialog to show when scan certificate failed */
    private void showAttentionDialog(String sms) {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.error_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText(sms);
        applicant_name.setText("Attention");
        description.setText("Verification Failed");

        // Get scanned date and time for offline certificate num
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

    /* Error dialog to show when scan certificate failed */
    private void showAttentionDepartureDialog(String sms) {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.error_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);

        message.setText(sms);
        applicant_name.setText(R.string.attention);
        description.setText(R.string.verification_failed);

        // Get scanned date and time for offline certificate num
        String scannedDate = dateFormatter.format(date);
        String scannedTime = timeFormatter.format(date);
        String referenceNumber = "CT-1300" + scannedTime;
        offlineDB.insertInvalidDeparture(referenceNumber, scannedDate, scannedTime);
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

    // Error dialog to show when scan arrival or departure failed //
    private void showCheckedDialog(String applicantName, String referenceNumber, String status, int flag) {
        checkedOutDialog = new Dialog(this);
        checkedOutDialog.setCanceledOnTouchOutside(false);
        checkedOutDialog.setContentView(R.layout.error_dialog);

        TextView message = checkedOutDialog.findViewById(R.id.message_title);
        TextView applicant_name = checkedOutDialog.findViewById(R.id.name_title);
        TextView description = checkedOutDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = checkedOutDialog.findViewById(R.id.agree_button);
        applicant_name.setText("Applicant " + applicantName);
        message.setText(status);
        description.setText("With Reference " + referenceNumber);


        dismissButton.setOnClickListener(view -> {
            if(Objects.equals(flag, 1)){
                offlineDB.insertFailedCheckins(referenceNumber, applicantName, status, formatter.format(date));
            } else {
                offlineDB.insertFailedCheckins(referenceNumber, applicantName, status, formatter.format(date));
            }
            checkedOutDialog.dismiss();
        });
        checkedOutDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        checkedOutDialog.show();
    }

    // Online Method for verify Qr Code for searchCertificateReference
    private void onlineArrivalSearchCertificateReference(String data){
        searchDialog = ProgressDialog.show(HomeActivity.this, "Processing", "Please wait...");
        StringRequest request = new StringRequest(Request.Method.POST, Config.GET_APPLICANT,
                response -> {
                    searchDialog.dismiss();
                    System.out.println("ZIC scan Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject applicantResponse = jsonObject.getJSONObject("response");
                        JSONObject applicantData = jsonObject.getJSONObject("data");
                        String code = applicantResponse.getString("code");
                        String message = applicantResponse.getString("message");

                        if(code.equals("200")) {
                            Log.e(Config.LOG_TAG, String.valueOf(applicantData));
                            applicantName = applicantData.getString("name");
                            referenceNumber = applicantData.getString("reference_number");
                            nationality = applicantData.getString("nationality");
                            arrivalDate = applicantData.getString("arrival_date");
                            birthDate = applicantData.getString("birth_date");
                            passportNumber = applicantData.getString("passport_number");
                            applicationStatus  = applicantData.getString("insurance_status");

                            LocalDate todayDate = LocalDate.now();
                            LocalDate insuranceStartDate = LocalDate.parse(arrivalDate);

                            if(Objects.equals(applicationStatus, "Expired")){
                                showErrorDialog("The Insurance certificate has expired");
                            }else if(todayDate.isBefore(insuranceStartDate)){
                                    showAttentionDialog("Insurance is not active until " + arrivalDate);
                            }else if(Objects.equals(applicationStatus, "In Use")){
                                showAttentionDialog("The visitor has already scanned on arrival");
                            }else if(Objects.equals(applicationStatus, "Seized")){
                                showAttentionDialog("The visitor has already scanned on departure");
                            } else {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Config.INCOMING_TAG, tag);
                                    bundle.putString("applicant_name", applicantName);
                                    bundle.putString("reference_number", referenceNumber);
                                    bundle.putString("nationality", nationality);
                                    bundle.putString("arrival_date", arrivalDate);
                                    bundle.putString("birth_date", birthDate);
                                    bundle.putString("passport_number", passportNumber);
                                    bundle.putString("application_status", applicationStatus);
                                    bundle.putString("flag", "Arrival");

                                    Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                                    intent.putExtras(bundle);
                                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }

                        } else {
                            showSnackBar("Failed to get Applicant: " + message);
                        }

                    } catch (JSONException exception) {
                        showSnackBar("Request Error: " + exception);
                    }
                },
                error -> {
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
                params.put("reference_number", Config.removeDoubleQuotes(data));
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

    // Offline Method for verify Qr Code for searchCertificateReference
    private void offlineArrivalSearchCertificateReference(String data) {
        JSONObject applicationData =  offlineDB.getApplication(Config.removeDoubleQuotes(data));
        try {
            String name = applicationData.getString("name");
            String reference_number = applicationData.getString("reference_number");
            String nationality = applicationData.getString("nationality");
            String arrival_date = applicationData.getString("arrival_date");
            String passport_number = applicationData.getString("passport_number");
            String birth_date = applicationData.getString("birth_date");
            String application_status = applicationData.getString("application_status");
            System.out.println("Insurance " + application_status);
            LocalDate todayDate = LocalDate.now();

            LocalDate insuranceStartDate = LocalDate.parse(arrival_date);

            if(application_status.equals("Expired")){
                showErrorDialog("The Insurance certificate has expired");
            }else if(application_status.equals("In Use")){
                    showErrorDialog("The Visitor has already scanned on arrival");
            }else if(application_status.equals("Seized")){
                showErrorDialog("The Visitor has already scanned on departure");
            }else if(todayDate.isBefore(insuranceStartDate)){
                showAttentionDialog("The Insurance is not active until " + arrival_date);
            }else{
                    Bundle bundle = new Bundle();
                    bundle.putString(Config.INCOMING_TAG, tag);
                    bundle.putString("name", name);
                    bundle.putString("reference_number", reference_number);
                    bundle.putString("nationality", nationality);
                    bundle.putString("arrival_date",arrival_date);
                    bundle.putString("birth_date",birth_date);
                    bundle.putString("passport_number", passport_number);
                    bundle.putString("application_status", application_status);
                    bundle.putString("flag", "Arrival");

                    Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                    intent.putExtras(bundle);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

        } catch (JSONException exception) {
            Log.e("Error", exception.getMessage());
            showSnackBar("Invalid Qr code");
        }
    }

    // Method to sync the offline request to online
    private void syncCertificatesOfflineRequest() {
        if(isOnline(this)){
            List<OfflineCertificatesRequest> requests = requestDAO.getAllRequests();
            for(OfflineCertificatesRequest request : requests ) {
                 sendCertificatesOfflineRequest(request);
            }
        }
    }

    // Method to sync the offline request to online
    private void syncDepartureCertificatesOfflineRequest() {
        if(isOnline(this)){
            List<OfflineDepartureRequest> requests = requestDAO.getDepartureAllRequests();
            for(OfflineDepartureRequest request : requests ) {
                departureSendCertificatesOfflineRequest(request);
            }
        }
    }

    // Request to send arrival data to API when device comes ONLINE
    private void sendCertificatesOfflineRequest(OfflineCertificatesRequest request) {
        StringRequest certificateRequest = new StringRequest(Request.Method.POST, Config.MARK_IN_USE,
                response -> {
                    System.out.println("Request = " + request);
                   Log.e("CERTIFICATE RESPONSE: ", String.valueOf(response));
                   try{
                       JSONObject jsonObject = new JSONObject(String.valueOf(response));
                       JSONObject certificateResponse = jsonObject.getJSONObject("response");
                       String code = certificateResponse.getString("code");
                       String message =certificateResponse.getString("message");

                       if(code.equals("200")){
                           JSONObject certificateDetails = jsonObject.getJSONObject("data");

                           Log.e("CERTIFICATE OFFLINE DETAILS: ", String.valueOf(certificateDetails));
                           requestDAO.deleteRequest(request.getId());
                           System.out.println("DATA SENT OFFLINE SUCCESSFULLY");
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
                       showSnackBar("" + error);
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
        certificateRequest.setRetryPolicy(new DefaultRetryPolicy (4000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(certificateRequest);
    }

    // Request to send departure data to API when device comes ONLINE
    private void departureSendCertificatesOfflineRequest(OfflineDepartureRequest request) {
        StringRequest certificateRequest = new StringRequest(Request.Method.POST, Config.MARK_SEIZED,
                response -> {
                    System.out.println("Request = " + request);
                    Log.e("OFFLINE DEPARTURE SYNC CERTIFICATE RESPONSE: ", String.valueOf(response));
                    try{
                        JSONObject jsonObject = new JSONObject(String.valueOf(response));
                        JSONObject certificateResponse = jsonObject.getJSONObject("response");
                        String code = certificateResponse.getString("code");
                        String message =certificateResponse.getString("message");

                        if(code.equals("200")){
                            JSONObject certificateDetails = jsonObject.getJSONObject("data");

                            Log.e("DEPARTURE CERTIFICATE OFFLINE DETAILS: ", String.valueOf(certificateDetails));
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
                    showSnackBar("" + error);
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
        certificateRequest.setRetryPolicy(new DefaultRetryPolicy (4000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(certificateRequest);
    }

    // Online Method for verify Qr Code for searchCertificateReference on Departure
    private void onlineDepartureSearchCertificateReference(String data){
        searchDialog = ProgressDialog.show(HomeActivity.this, "Processing", "Please wait...");
        StringRequest request = new StringRequest(Request.Method.POST, Config.GET_APPLICANT,
                response -> {
                    searchDialog.dismiss();
                    System.out.println("Departure ZIC scan Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject applicantResponse = jsonObject.getJSONObject("response");
                        JSONObject applicantData = jsonObject.getJSONObject("data");
                        String code = applicantResponse.getString("code");
                        String message = applicantResponse.getString("message");

                        if(code.equals("200")) {
                            Log.e(Config.LOG_TAG, String.valueOf(applicantData));
                            applicantName = applicantData.getString("name");
                            referenceNumber = applicantData.getString("reference_number");
                            nationality = applicantData.getString("nationality");
                            arrivalDate = applicantData.getString("arrival_date");
                            birthDate = applicantData.getString("birth_date");
                            passportNumber = applicantData.getString("passport_number");
                            applicationStatus  = applicantData.getString("insurance_status");

                            LocalDate todayDate = LocalDate.now();
                            LocalDate insuranceStartDate = LocalDate.parse(arrivalDate);
                            System.out.println("Comparison date: " + applicationStatus);
                            if(Objects.equals(applicationStatus, "Expired")){
                                showAttentionDepartureDialog("The Insurance certificate has already expired");
                            }else if(Objects.equals(applicationStatus, "Seized")){
                                showAttentionDepartureDialog("The visitor has already scanned on departure");
                            }else if(todayDate.isBefore(insuranceStartDate)){
                                showAttentionDepartureDialog("The Insurance is not active until " + arrivalDate);
                            } else if(Objects.equals(applicationStatus, "In Use")) {
                                Bundle bundle = new Bundle();
                                bundle.putString(Config.INCOMING_TAG, tag);
                                bundle.putString("applicant_name", applicantName);
                                bundle.putString("reference_number", referenceNumber);
                                bundle.putString("nationality", nationality);
                                bundle.putString("arrival_date", arrivalDate);
                                bundle.putString("birth_date", birthDate);
                                bundle.putString("passport_number", passportNumber);
                                bundle.putString("application_status", applicationStatus);
                                bundle.putString("flag", "Departure");

                                Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                                intent.putExtras(bundle);
                                intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                showAttentionDepartureDialog("The visitor has not scanned upon arrival");
                            }

                        } else {
                            showSnackBar("Failed to get Applicant: " + message);
                        }

                    } catch (JSONException exception) {
                        showSnackBar("Request Error: " + exception);
                    }
                },
                error -> {
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
                params.put("reference_number", Config.removeDoubleQuotes(data));
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

    // Offline Method for verify Qr Code for searchCertificateReference on Departure
    private void offlineDepartureSearchCertificateReference(String data) {
        JSONObject applicationData =  offlineDB.getApplication(Config.removeDoubleQuotes(data));
        System.out.println("Tupo offline now");
        try {
            String name = applicationData.getString("name");
            String reference_number = applicationData.getString("reference_number");
            String nationality = applicationData.getString("nationality");
            String arrival_date = applicationData.getString("arrival_date");
            String passport_number = applicationData.getString("passport_number");
            String birth_date = applicationData.getString("birth_date");
            String application_status = applicationData.getString("application_status");
            System.out.println("Insurance " + application_status);
            LocalDate todayDate = LocalDate.now();

            LocalDate insuranceStartDate = LocalDate.parse(arrival_date);

            if(application_status.equals("Expired")){
                showAttentionDepartureDialog("Insurance certificate expired");
            }else if(application_status.equals("Seized")){
                showAttentionDepartureDialog("Visitor already depart");
            }else if(todayDate.isBefore(insuranceStartDate)){
                showAttentionDepartureDialog("Your insurance will be active on " + arrival_date);
            }else if(application_status.equals("In Use")){
                Bundle bundle = new Bundle();
                bundle.putString(Config.INCOMING_TAG, tag);
                bundle.putString("name", name);
                bundle.putString("reference_number", reference_number);
                bundle.putString("nationality", nationality);
                bundle.putString("arrival_date",arrival_date);
                bundle.putString("birth_date",birth_date);
                bundle.putString("passport_number", passport_number);
                bundle.putString("application_status", application_status);
                bundle.putString("flag", "Departure");

                Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                intent.putExtras(bundle);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                showAttentionDepartureDialog("The visitor has not scanned upon arrival");
            }

        } catch (JSONException exception) {
            Log.e("Error", exception.getMessage());
            showSnackBar("Invalid Qr code");
        }
    }
}