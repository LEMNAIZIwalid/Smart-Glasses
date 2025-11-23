package com.example.lado.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lado.Models.NotificationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseService {

    private static final String TAG = "FirebaseService";
    private final String uid;
    private final Context context; // Contexte pour les Toasts

    private String lastCameraStatus = "--";
    private String lastCurrentDistance = "--";

    public FirebaseService(Context context, String uid) {
        this.context = context;
        this.uid = uid;
    }

    public void startListening() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("sensors");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // -------- CAMERA --------
                Object cameraObj = snapshot.child("camera").child("status").getValue();
                if (cameraObj != null) {
                    String cameraStatus = cameraObj.toString();
                    if (!cameraStatus.equals(lastCameraStatus)) {
                        if (!lastCameraStatus.equals("--")) {
                            showToast("État de la caméra : " + cameraStatus);
                            saveNotification("camera", "Caméra : " + cameraStatus);
                        }
                        lastCameraStatus = cameraStatus;
                    }
                }

                // -------- CURRENT DISTANCE --------
                Object currentDistObj = snapshot.child("current").child("distance").getValue();
                if (currentDistObj != null) {
                    String currentDist = currentDistObj.toString();
                    if (!currentDist.equals(lastCurrentDistance)) {
                        if (!lastCurrentDistance.equals("--")) {
                            showToast("Obstacle détecté à " + currentDist + " cm");
                            saveNotification("current", "Distance courante : " + currentDist + " cm");
                        }
                        lastCurrentDistance = currentDist;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    // Affiche un Toast simple
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Ajoute la notification dans Firebase
    private void saveNotification(String type, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications")
                .child(type);

        String notifId = notifRef.push().getKey();
        if (notifId == null) return;

        NotificationModel notif = new NotificationModel(message, System.currentTimeMillis());
        notifRef.child(notifId).setValue(notif);
    }
}
