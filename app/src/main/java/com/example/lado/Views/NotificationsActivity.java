package com.example.lado.Views;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lado.Models.NotificationModel;
import com.example.lado.R;
import com.example.lado.adapters.NotificationAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications = new ArrayList<>();
    private String uid;

    private static final String TAG = "NotificationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_notifications) return true;
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_statics) {
                startActivity(new Intent(this, StaticsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        recyclerView.setAdapter(adapter);

        // Firebase
        uid = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");
        if (uid != null && !uid.isEmpty()) {
            loadNotificationsFromFirebase();
        }
    }

    private void loadNotificationsFromFirebase() {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications");

        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();

                for (DataSnapshot typeSnap : snapshot.getChildren()) {
                    for (DataSnapshot notifSnap : typeSnap.getChildren()) {

                        Object value = notifSnap.getValue();

                        if (value instanceof NotificationModel) {
                            notifications.add((NotificationModel) value);
                        } else if (value instanceof String) {
                            // ancien format string
                            notifications.add(new NotificationModel((String) value, System.currentTimeMillis()));
                        } else if (value instanceof Long) {
                            // ancien format timestamp seul
                            notifications.add(new NotificationModel("Notification", (Long) value));
                        } else if (value instanceof java.util.Map) {
                            // cas classique Firebase
                            try {
                                java.util.Map map = (java.util.Map) value;
                                String message = map.get("message") != null ? map.get("message").toString() : "Notification";
                                long timestamp = map.get("timestamp") != null ? Long.parseLong(map.get("timestamp").toString()) : System.currentTimeMillis();
                                notifications.add(new NotificationModel(message, timestamp));
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur conversion notification: " + e.getMessage());
                            }
                        } else {
                            // fallback
                            notifications.add(new NotificationModel("Notification inconnue", System.currentTimeMillis()));
                        }
                    }
                }

                // Trier par timestamp décroissant
                notifications.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                adapter.setNotifications(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }
}
