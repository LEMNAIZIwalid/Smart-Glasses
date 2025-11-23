package com.example.lado.Utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.lado.R;

import android.os.Handler;

public class NotificationBanner {

    private static View bannerView;

    public static void show(Activity activity, String message) {

        if (bannerView != null) return; // Empêcher plusieurs bannières empilées

        FrameLayout root = activity.findViewById(android.R.id.content);

        bannerView = LayoutInflater.from(activity).inflate(R.layout.notification_banner, root, false);
        TextView textMessage = bannerView.findViewById(R.id.bannerMessage);
        textMessage.setText(message);

        // Ajouter la bannière au layout root
        root.addView(bannerView);

        // Disparition après 3 secondes
        new Handler().postDelayed(() -> {
            try {
                root.removeView(bannerView);
            } catch (Exception ignored) {}
            bannerView = null;
        }, 3000);
    }
}
