package com.example.lado.Views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lado.R;
import com.example.lado.Models.NotificationModel;
import com.example.lado.adapters.NotificationAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter adapter;
    private final List<NotificationModel> notificationsList = new ArrayList<>();
    private String uid;
    private static final String TAG = "NotificationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));

        uid = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "");

        adapter = new NotificationAdapter(notificationsList);
        recyclerNotifications.setAdapter(adapter);

        setupBottomNavigation();
        if (uid != null && !uid.isEmpty()) {
            loadNotificationsFromFirebase();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_historique) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_livestream) intent = new Intent(this, LivestreamActivity.class);
            else if (id == R.id.nav_statics) intent = new Intent(this, StaticsActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_notifications;
        });
    }

    private void loadNotificationsFromFirebase() {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("notifications");

        notifRef.addValueEventListener(new ValueEventListener() { // listener continu
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsList.clear();
                long now = System.currentTimeMillis();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel notif = ds.getValue(NotificationModel.class);
                    if (notif != null) {
                        // Supprimer si > 24h
                        if (now - notif.getTimestamp() > 24 * 60 * 60 * 1000) {
                            ds.getRef().removeValue();
                        } else {
                            notificationsList.add(notif);
                        }
                    }
                }

                // Trier par timestamp décroissant
                Collections.sort(notificationsList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur Firebase: " + error.getMessage());
            }
        });
    }
}
