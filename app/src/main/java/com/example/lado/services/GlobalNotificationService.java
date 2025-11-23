package com.example.lado.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GlobalNotificationService extends Service {

    private static final String TAG = "GlobalNotificationService";
    private FirebaseService firebaseService;

    @Override
    public void onCreate() {
        super.onCreate();
        String uid = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", null);

        if (uid != null && !uid.isEmpty()) {
            firebaseService = new FirebaseService(getApplicationContext(), uid);
            firebaseService.startListening();
            Log.d(TAG, "FirebaseService started for UID: " + uid);
        } else {
            Log.e(TAG, "UID not found, cannot start FirebaseService");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
