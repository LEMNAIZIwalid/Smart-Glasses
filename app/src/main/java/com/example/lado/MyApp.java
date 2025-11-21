package com.example.lado;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyApp extends Application {

    private static MyApp instance;
    private long lastTimestamp = 0;
    private String uid;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Charger UID
        uid = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "");

        if (!uid.isEmpty()) {
            listenForNotifications();
        }
    }

    public static MyApp getInstance() {
        return instance;
    }

    private void listenForNotifications() {

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications");

        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Long ts = ds.child("timestamp").getValue(Long.class);
                    String msg = ds.child("message").getValue(String.class);

                    if (ts == null || msg == null) continue;

                    // Nouveau ➝ afficher alerte !
                    if (ts > lastTimestamp) {
                        lastTimestamp = ts;
                        ActivityManager.showAlert(msg);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("MyApp", error.getMessage());
            }
        });
    }
}
