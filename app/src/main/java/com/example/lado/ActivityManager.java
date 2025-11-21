package com.example.lado;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class ActivityManager {

    private static Activity currentActivity;

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static void showAlert(String msg) {
        if (currentActivity == null) return;

        View root = currentActivity.findViewById(android.R.id.content);
        if (root == null) return;

        Snackbar snackbar = Snackbar.make(root, msg, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(0xFF14213D); // Bleu foncé
        snackbar.setTextColor(0xFFFFFFFF);      // Blanc
        snackbar.show();
    }
}
