package com.example.zicapp.claimInspector;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.common.apiutil.decode.DecodeReader;
import com.common.callback.IDecodeReaderListener;
import com.example.zicapp.HomeActivity;
import com.example.zicapp.R;
import com.example.zicapp.ResultActivity;
import com.example.zicapp.SettingsActivity;
import com.example.zicapp.dialog.LoadingDialog;
import com.example.zicapp.utils.Config;
import com.example.zicapp.utils.OfflineDB;
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

public class ClaimInspectorActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(ClaimInspectorActivity.this);
    private ProgressDialog searchDialog;
    DecodeReader reader;
    MaterialCardView scanInsurance;
    MaterialCardView settingsCard;
    MaterialCardView reportCard;
    TextView totalClaims;
    TextView inspectorName;
    TextView todayDate;
    TextView entryPoint;

    SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    Date date = new Date();

    private long lastScanTime = 0;
    private static final int SCAN_DELAY = 2000; // 2 seconds

    String applicantName;
    String referenceNumber;
    String nationality;
    String arrivalDate;
    String birthDate;
    String passportNumber;
    String applicationStatus;
    String entry_point;
    int total_claims;
    public String username;
    public String authToken;
    private String tag;
    boolean isScanner = false;

    private Dialog checkedOutDialog;
    View parentLayout;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_inspector);
        prepareData();
        loadingDialog = new LoadingDialog();
        parentLayout = findViewById(android.R.id.content);
        scanInsurance = findViewById(R.id.scan_insurance);
//        reportCard = findViewById(R.id.report_card);
        settingsCard = findViewById(R.id.settings_card);
        totalClaims = findViewById(R.id.total_claims);
        todayDate = findViewById(R.id.today_date);
        inspectorName = findViewById(R.id.officer_name);
        entryPoint = findViewById(R.id.entry_point);

        total_claims = offlineDB.totalClaims();
        todayDate.setText(dateFormatter2.format(date));
        inspectorName.setText(username);
        entryPoint.setText(entry_point);

        totalClaims.setText(String.valueOf(total_claims));
        reader = new DecodeReader(this);

        scanInsurance.setOnClickListener(v -> {
            if (isScanner) {
                // Ignore clicks if already processing from reader
                return;
            } else {
                if(checkPackage("com.telpo.tps550.api")) {
                    Intent intent = new Intent();
                    intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                    verificationScan.launch(intent);

                } else  {

                    Snackbar snackbar;
                    snackbar = Snackbar.make(parentLayout, "Invalid Device Please Use Scanners", Snackbar.LENGTH_SHORT);
                    View snack_view = snackbar.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)snack_view.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    snack_view.setBackgroundColor(0xFFe56b6f);
                    snackbar.show();
                }
            }
        });

        settingsCard.setOnClickListener(v -> {
            Intent i = new Intent(ClaimInspectorActivity.this, SettingsActivity.class);
            startActivity(i);
        });

        reader.setDecodeReaderListener(new IDecodeReaderListener() {
            @Override
            public void onRecvData(byte[] data) {
                playSound(R.raw.success_beep);
                String part = new String(data);
                String permit_reference = Config.removeDoubleQuotes(part);

                long now = System.currentTimeMillis();
                if (now - lastScanTime > SCAN_DELAY) {
                    lastScanTime = now;

                    searchInsurance(permit_reference);
                } else {
                    // Ignore duplicate scan within 2 seconds
                    Log.d("Scanner", "Ignored duplicate scan: " + permit_reference);
                }
            }
        });

    }

    // Prepare Data for Home Page Display
    private void prepareData() {
        Bundle bundle = null;
        bundle = this.getIntent().getExtras();
        SharedPreferences preferences = ClaimInspectorActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(Config.USER_NAME, "n.a");
        authToken = preferences.getString(Config.AUTH_TOKEN, "n.a");
        entry_point = preferences.getString(Config.ENTRYPOINT, "n.a");
    }

    private boolean checkPackage(String packageName) {
        PackageManager manager = this.getPackageManager();
        Intent intent = new Intent().setPackage(packageName);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        if (infos == null || infos.size() < 1) {
            return false;
        } else {
            return true;
        }
    }

    ActivityResultLauncher<Intent> verificationScan = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == RESULT_OK) {
                    Toast t = Toast.makeText(getApplicationContext(), "Qr Code haipatikani", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
                    t.show();
                } else if(result.getResultCode() == RESULT_CANCELED) {
                    if(result.getData() != null) {
                        String data = result.getData().getStringExtra("qrCode");
                        System.out.println("Result Cancelled Data: "  + Config.removeDoubleQuotes(data));
                        String permit_reference = Config.removeDoubleQuotes(data);
                        searchInsurance(permit_reference);
                    }
                }
            }
    );


    private void searchInsurance(String data){
        loadingDialog.show(this, "Processing... Please wait");
        StringRequest request = new StringRequest(Request.Method.POST, Config.GET_APPLICANT,
                response -> {
                    loadingDialog.hide();
                    System.out.println("Search Insurance response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject applicantResponse = jsonObject.getJSONObject("response");
                        String code = applicantResponse.getString("code");
                        String message = applicantResponse.getString("message");
                        System.out.println("Code: " + code);
                        if(code.equals("200")) {
                            JSONObject applicantData = jsonObject.getJSONObject("data");
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
                            }else if(Objects.equals(applicationStatus, "Pending Payment")){
                                showErrorDialog("The applicant has not completed the payment");
                            }else if(Objects.equals(applicationStatus, "Dismissed")){
                                showErrorDialog("Invalid, insurance dismissed");
                            }else if(Objects.equals(applicationStatus, "Pending Approval")){
                                showErrorDialog("Invalid, Insurance is Pending Approval");
                            }else {
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
                                bundle.putString("connectivity", "offline");

                                JSONObject _applicationObject = new JSONObject();
                                _applicationObject.put("name", applicantName);
                                _applicationObject.put("reference_number", referenceNumber);
                                _applicationObject.put("nationality", nationality);
                                _applicationObject.put("arrival_date",  arrivalDate);
                                _applicationObject.put("birth_date",  birthDate);
                                _applicationObject.put("passport_number", passportNumber);
                                _applicationObject.put("application_status", applicationStatus);



                                Intent intent = new Intent(ClaimInspectorActivity.this, ClaimResultActivity.class);
                                intent.putExtras(bundle);
                                intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                        } else {
                            showSnackBar("Failed to get Applicant: " + message);
                        }

                    } catch (JSONException exception) {
                        showSnackBar("Invalid qr code");
                    }
                },
                error -> {
                    if(String.valueOf(error).equals("com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"earrival.rahisi.co.tz\": No address associated with hostname")){
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
                params.put("applicant_travel_type", "1");
                System.out.println("Parameter za GET: ✨ " + params);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

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

        dismissButton.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(Config.INCOMING_TAG, tag);
            bundle.putInt("scan_flag", 1);
            checkedOutDialog.dismiss();
            Intent intent = new Intent(ClaimInspectorActivity.this, ClaimInspectorActivity.class);
            intent.putExtras(bundle);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        checkedOutDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        checkedOutDialog.show();
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


    private void playSound(int resId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.reset();
            mp.release();
        });
        mediaPlayer.start();
    }
}