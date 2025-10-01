package com.example.zicapp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.zicapp.R;

public class LoadingDialog {
    private AlertDialog dialog;

    public void show(Activity activity, String message) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return; // don't show if activity is not valid
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_loading, null);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
