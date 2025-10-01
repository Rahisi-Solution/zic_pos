package com.example.zicapp.claimInspector;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import models.AccidentTypeModel;
import models.ComplainModel;
import models.RepatriationStatusModel;
import models.RepatriationTypeModel;

public class ClaimFormActivity extends AppCompatActivity {
    OfflineDB offlineDB = new OfflineDB(ClaimFormActivity.this);
    Spinner spinnerHospital, spinnerComplain, spinnerAccidentType, spinnerRepatriationType, spinnerRepatriationStatus;
    EditText inputAdmissionNumber, inputDays, inputComment;
    Button btnSubmit;
    TextView repatriationTypeHeader;
    View parentLayout;
    private Dialog successDialog;

    JSONArray hospitals;
    JSONArray complains;
    JSONArray accident_type;
    JSONArray repatriation_type;
    JSONArray repatriation_status;

    String auth_token;
    String zicNumber;
    private ProgressDialog submitProgress;

    //    List<HospitalsModel> hospitalsData = new ArrayList<>();
    List<ComplainModel> complainData = new ArrayList<>();
    List<AccidentTypeModel> accidentTypeData = new ArrayList<>();
    List<RepatriationTypeModel> repatriationTypeData = new ArrayList<>();
    List<RepatriationStatusModel> repatriationStatusData = new ArrayList<>();

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    Date date = new Date();
    String scannedDate = dateFormatter.format(date);
    String scannedTime = timeFormatter.format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_form);

        // Initialize Views
//        spinnerHospital = findViewById(R.id.spinner_hospital);
        spinnerComplain = findViewById(R.id.spinner_complain);
        spinnerAccidentType = findViewById(R.id.spinner_accident_type);
        spinnerRepatriationType = findViewById(R.id.spinner_repatriation_type);
        spinnerRepatriationStatus = findViewById(R.id.spinner_repatriation_status);
        repatriationTypeHeader = findViewById(R.id.repatriation_type_header);

        inputAdmissionNumber = findViewById(R.id.input_admission_number);
        inputDays = findViewById(R.id.input_days);
        inputComment = findViewById(R.id.input_comment);
        btnSubmit = findViewById(R.id.btn_submit);

//        hospitalsData.add(new HospitalsModel("", "Choose Hospital", ""));
        complainData.add(new ComplainModel("", "Choose Complain", ""));
        accidentTypeData.add(new AccidentTypeModel("", "Choose Type Of Case", ""));
        repatriationTypeData.add(new RepatriationTypeModel("", "Choose Repatriation Type", ""));
        repatriationStatusData.add(new RepatriationStatusModel("", "Choose Repatriation Status", ""));
        parentLayout = findViewById(android.R.id.content);

        prepareData();
//        prepareHospitalData();
        prepareComplainData();
        prepareAccidentData();
        prepareRepatriationStatusData();
        prepareRepatriationTypeData();

        // Show/hide repatriation status dropdown
        spinnerRepatriationStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Yes".equalsIgnoreCase(selected)) {
                    repatriationTypeHeader.setVisibility(View.VISIBLE);
                    spinnerRepatriationType.setVisibility(View.VISIBLE);
                } else {
                    repatriationTypeHeader.setVisibility(View.GONE);
                    spinnerRepatriationType.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Submit button action
        btnSubmit.setOnClickListener(v -> {
            validateForm();
        });
    }

    void prepareData(){
        SharedPreferences preferences = ClaimFormActivity.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        auth_token = preferences.getString(Config.AUTH_TOKEN, "n/a");
        Bundle bundle = null;
        bundle = this.getIntent().getExtras();
        assert bundle != null;
        zicNumber = bundle.getString("reference_number");
        try {
//            hospitals = new JSONArray(preferences.getString(Config.HOSPITALS, "n.a"));
            complains = new JSONArray(preferences.getString(Config.COMPLAIN, "n.a"));
            accident_type = new JSONArray(preferences.getString(Config.ACCIDENT_TYPE, "n.a"));
            repatriation_type = new JSONArray(preferences.getString(Config.REPATRIATION_TYPE, "n.a"));
            repatriation_status = new JSONArray(preferences.getString(Config.REPATRIATION_STATUS, "n.a"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

//    void prepareHospitalData(){
//        for (int i = 0; i < hospitals.length(); i++) {
//            try {
//                JSONObject hospitalObj = hospitals.getJSONObject(i);
//                String id = hospitalObj.getString("id");
//                String name = hospitalObj.getString("name");
//                String rowId = hospitalObj.getString("row_id");
//
//
//                hospitalsData.add(new HospitalsModel(id, name, rowId));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        ArrayAdapter<HospitalsModel> hospitalAdapter = new ArrayAdapter<HospitalsModel>(this, android.R.layout.simple_spinner_item, hospitalsData) {
//            @Override
//            public boolean isEnabled(int position) {
//                return position != 0; // disable hint
//            }
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                View view = super.getView(position, convertView, parent);
//                TextView text = (TextView) view.findViewById(android.R.id.text1);
//
//                if (position == 0) {
//                    text.setTextColor(Color.GRAY); // hint
//                } else {
//                    text.setTextColor(getResources().getColor(R.color.primary_color));
//                }
//                return view;
//            }
//
//            @Override
//            public View getDropDownView(int position, View convertView, ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                if (position == 0) {
//                    tv.setTextColor(Color.GRAY);
//                } else {
//                    tv.setTextColor(Color.BLACK);
//                }
//                return view;
//            }
//        };
//
//        hospitalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        spinnerHospital.setAdapter(hospitalAdapter);
//        spinnerHospital.setDropDownVerticalOffset(10);
//        spinnerHospital.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//    }

    void prepareComplainData(){
        for (int i = 0; i < complains.length(); i++) {
            try {
                JSONObject complainObj = complains.getJSONObject(i);
                String id = complainObj.getString("id");
                String name = complainObj.getString("name");
                String rowId = complainObj.getString("row_id");


                complainData.add(new ComplainModel(id, name, rowId));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<ComplainModel> complainAdapter = new ArrayAdapter<ComplainModel>(this, android.R.layout.simple_spinner_item, complainData) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable hint
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(Color.GRAY); // hint
                } else {
                    text.setTextColor(getResources().getColor(R.color.primary_color));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        complainAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerComplain.setAdapter(complainAdapter);
        spinnerComplain.setDropDownVerticalOffset(10);
        spinnerComplain.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    void prepareAccidentData(){
        for (int i = 0; i < accident_type.length(); i++) {
            try {
                JSONObject accidentObj = accident_type.getJSONObject(i);
                String id = accidentObj.getString("id");
                String name = accidentObj.getString("name");
                String rowId = accidentObj.getString("row_id");


                accidentTypeData.add(new AccidentTypeModel(id, name, rowId));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<AccidentTypeModel> accidentTypeAdapter = new ArrayAdapter<AccidentTypeModel>(this, android.R.layout.simple_spinner_item, accidentTypeData) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable hint
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(Color.GRAY); // hint
                } else {
                    text.setTextColor(getResources().getColor(R.color.primary_color));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        accidentTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerAccidentType.setAdapter(accidentTypeAdapter);
        spinnerAccidentType.setDropDownVerticalOffset(10);
        spinnerAccidentType.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    void prepareRepatriationStatusData(){
        for (int i = 0; i < repatriation_status.length(); i++) {
            try {
                JSONObject repatriationObj = repatriation_status.getJSONObject(i);
                String id = repatriationObj.getString("id");
                String name = repatriationObj.getString("name");
                String rowId = repatriationObj.getString("row_id");


                repatriationStatusData.add(new RepatriationStatusModel(id, name, rowId));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<RepatriationStatusModel> repatriationAdapter = new ArrayAdapter<RepatriationStatusModel>(this, android.R.layout.simple_spinner_item, repatriationStatusData) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable hint
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(Color.GRAY); // hint
                } else {
                    text.setTextColor(getResources().getColor(R.color.primary_color));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        repatriationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerRepatriationStatus.setAdapter(repatriationAdapter);
        spinnerRepatriationStatus.setDropDownVerticalOffset(10);
        spinnerRepatriationStatus.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    void prepareRepatriationTypeData(){
        for (int i = 0; i < repatriation_type.length(); i++) {
            try {
                JSONObject repatriationObj = repatriation_type.getJSONObject(i);
                String id = repatriationObj.getString("id");
                String name = repatriationObj.getString("name");
                String rowId = repatriationObj.getString("row_id");


                repatriationTypeData.add(new RepatriationTypeModel(id, name, rowId));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<RepatriationTypeModel> repatriationAdapter = new ArrayAdapter<RepatriationTypeModel>(this, android.R.layout.simple_spinner_item, repatriationTypeData) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable hint
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(Color.GRAY); // hint
                } else {
                    text.setTextColor(getResources().getColor(R.color.primary_color));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        repatriationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerRepatriationType.setAdapter(repatriationAdapter);
        spinnerRepatriationType.setDropDownVerticalOffset(10);
        spinnerRepatriationType.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    void validateForm(){
//        int selectedHospitalIndex = spinnerHospital.getSelectedItemPosition();
        int selectedComplainIndex = spinnerComplain.getSelectedItemPosition();
        int selectedAccidentIndex = spinnerAccidentType.getSelectedItemPosition();
        int selectedStatusIndex = spinnerRepatriationStatus.getSelectedItemPosition();
        int selectedTypeIndex = spinnerRepatriationType.getSelectedItemPosition();
        String admissionNumber = inputAdmissionNumber.getText().toString().trim();
        String numberOfDays = inputDays.getText().toString().trim();
        String comment = inputComment.getText().toString().trim();

//        HospitalsModel selectedHospital = hospitalsData.get(selectedHospitalIndex);
        ComplainModel selectedComplain = complainData.get(selectedComplainIndex);
        AccidentTypeModel selectedAccident = accidentTypeData.get(selectedAccidentIndex);
        RepatriationStatusModel selectedRepatriationStatus = repatriationStatusData.get(selectedStatusIndex);
        RepatriationTypeModel selectedRepatriationType = repatriationTypeData.get(selectedTypeIndex);

//        String hospitalValue = selectedHospital.rowId;
        String complainValue = selectedComplain.rowId;
        String accidentTypeValue = selectedAccident.rowId;
        String repatriationStatusValue = selectedRepatriationStatus.rowId;
        String repatriationStatusName = selectedRepatriationStatus.name;
        String repatriationTypeValue = selectedRepatriationType.rowId;


//        if(selectedHospitalIndex == 0){
//            showSnackBar("Please Choose Hospital");
//            return;
//        }else
        if(selectedComplainIndex == 0){
            showSnackBar("Please Choose Type of Complain");
            return;
        }else if(selectedAccidentIndex == 0){
            showSnackBar("Please Choose Case Type");
            return;
        }else if(selectedStatusIndex == 0){
            showSnackBar("Please Choose Repatriation Status");
            return;
        }else if(Objects.equals(repatriationStatusName, "Yes") && selectedTypeIndex == 0){
            showSnackBar("Please Choose Repatriation Type");
            return;
        }else if(admissionNumber.isEmpty()){
            showSnackBar("Please Enter Admission Number");
            return;
        }else if(numberOfDays.isEmpty()){
            showSnackBar("Please Enter Number of Days");
            return;
        }else if(comment.isEmpty()){
            showSnackBar("Please Enter Comment");
            return;
        }

        if(Objects.equals(repatriationStatusName, "No")){
            repatriationTypeValue = "";
        }

        String finalRepatriationTypeValue = repatriationTypeValue;
        submitProgress = ProgressDialog.show(ClaimFormActivity.this, "Processing", "Please wait...");
        StringRequest request = new StringRequest(Request.Method.POST, Config.SUBMIT_CLAIM,
                response -> {
                    submitProgress.dismiss();
                    System.out.println("Claim Response: "+ response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject submitResponse = jsonObject.getJSONObject("response");
                        String code = submitResponse.getString("code");
                        String message = submitResponse.getString("message");

                        if(code.equals("200")){
                            offlineDB.insertClaim(zicNumber, scannedDate, scannedTime);
                            showSuccessDialog(message);
                        } else {
                            System.out.println("Something happened " + message);
                            showSnackBar(message);
                        }

                    } catch (JSONException exception) {
                        System.out.println("Exception: " + exception);
                    }
                },
                error -> {
                    try {
                        if(String.valueOf(error).equals("com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"earrival.rahisi.co.tz\": No address associated with hostname")){
                            System.out.println("The error HERE = " + error);
                        } else {
                        }
                    } catch (Exception e) {
                    }
                }){
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("authorization", "Bearer " + auth_token);
                params.put("reference_number", zicNumber);
                params.put("complain", complainValue);
                params.put("accident_type", accidentTypeValue);
                params.put("in_patient_status", numberOfDays);
//                params.put("hospital", hospitalValue);
                params.put("comments", comment);
                params.put("admission_number", admissionNumber);
                params.put("repatriation_status", repatriationStatusValue);
                params.put("repatriation_type", finalRepatriationTypeValue);
                System.out.println("Claim Params: " + params);
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

    private void showSuccessDialog(String txt_message) {
        successDialog = new Dialog(this);
        successDialog.setCanceledOnTouchOutside(false);
        successDialog.setContentView(R.layout.success_dialog);

        TextView message = successDialog.findViewById(R.id.message_title);
        TextView applicant_name = successDialog.findViewById(R.id.name_title);
        TextView description = successDialog.findViewById(R.id.desc_text);
        MaterialButton dismissButton = successDialog.findViewById(R.id.agree_button);

        message.setText(txt_message);
        applicant_name.setText("DONE");
        description.setText("");

        dismissButton.setOnClickListener(view -> {
            successDialog.dismiss();
            Intent intent = new Intent(ClaimFormActivity.this, ClaimInspectorActivity.class);
            startActivity(intent);
        });
        successDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        successDialog.show();
    }

}