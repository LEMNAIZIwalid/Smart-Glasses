package com.example.lado.Controller;

import android.content.Context;
import android.widget.TextView;

public class MainController {
    private Context context;

    public MainController(Context context) {
        this.context = context;
    }

    public void afficherMessage(TextView textView) {
        textView.setText("Bonjour d'abord");
    }
}