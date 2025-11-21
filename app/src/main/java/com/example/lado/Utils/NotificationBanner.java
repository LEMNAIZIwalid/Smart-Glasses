package com.example.lado.Utils;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.lado.R;

public class NotificationBanner {

    private static View bannerView;

    public static void show(Context context, String message) {

        if (bannerView != null) return; // Empêcher plusieurs bannières empilées

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        bannerView = LayoutInflater.from(context).inflate(R.layout.notification_banner, null);
        TextView textMessage = bannerView.findViewById(R.id.bannerMessage);
        textMessage.setText(message);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = 1;

        wm.addView(bannerView, params);

        // Disparition après 3 secondes
        new Handler().postDelayed(() -> {
            try {
                wm.removeView(bannerView);
            } catch (Exception ignored) {}
            bannerView = null;
        }, 3000);
    }
}
