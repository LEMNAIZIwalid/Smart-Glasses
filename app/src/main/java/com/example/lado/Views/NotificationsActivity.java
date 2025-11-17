package com.example.lado.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private SimpleNotificationAdapter adapter;
    private final List<String> notificationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // RecyclerView
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Charger notifications
        loadNotifications();

        adapter = new SimpleNotificationAdapter(notificationsList);
        recyclerNotifications.setAdapter(adapter);

        // BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_statics) {
                startActivity(new Intent(this, StaticsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadNotifications() {
        notificationsList.clear();
        notificationsList.add("📶 GSM Sensor signal improved — 2m ago");
        notificationsList.add("📡 GSM Module weak signal detected — 5m ago");
        notificationsList.add("⚠️ Alert: High temperature detected! — 15m ago");
        notificationsList.add("⚠️ Alert: Soil humidity too low! — 1h ago");
        notificationsList.add("📡 GSM Antenna connection restored — 10m ago");
    }

    private static class SimpleNotificationAdapter extends RecyclerView.Adapter<SimpleNotificationAdapter.ViewHolder> {

        private final List<String> notifications;

        SimpleNotificationAdapter(List<String> notifications) {
            this.notifications = notifications;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String notif = notifications.get(position);
            String[] parts = notif.split("—");
            String message = parts[0].trim();
            String time = parts.length > 1 ? parts[1].trim() : "";
            holder.textMessage.setText(message);
            holder.textTime.setText(time);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textMessage, textTime;

            ViewHolder(View itemView) {
                super(itemView);
                textMessage = itemView.findViewById(R.id.textMessage);
                textTime = itemView.findViewById(R.id.textTime);
            }
        }
    }
}
