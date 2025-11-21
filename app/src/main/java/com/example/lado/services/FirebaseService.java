package com.example.lado.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lado.Models.NotificationModel;
import com.example.lado.Utils.NotificationBanner;
import com.example.lado.Utils.NotificationBannerContext;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseService {

    private static final String TAG = "FirebaseService";
    private final String uid;

    public FirebaseService(String uid) {
        this.uid = uid;
    }

    public void startListening() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("sensors");

        userRef.addValueEventListener(new ValueEventListener() {
            private String lastCameraStatus = "--";
            private String lastCurrentDistance = "--";

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // Camera
                String cameraStatus = snapshot.child("camera").child("status").getValue(String.class);
                if (cameraStatus != null && !cameraStatus.equals(lastCameraStatus)) {
                    if (!lastCameraStatus.equals("--")) {
                        NotificationBanner.show(NotificationBannerContext.getContext(),
                                "État de la caméra : " + cameraStatus);
                    }
                    lastCameraStatus = cameraStatus;
                }

                // Current distance
                String currentDist = snapshot.child("current").child("distance").getValue(String.class);
                if (currentDist != null && !currentDist.equals(lastCurrentDistance)) {
                    if (!lastCurrentDistance.equals("--")) {
                        NotificationBanner.show(NotificationBannerContext.getContext(),
                                "Obstacle détecté à " + currentDist + " cm");
                    }
                    lastCurrentDistance = currentDist;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }
}
