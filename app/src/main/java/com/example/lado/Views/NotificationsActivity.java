package com.example.lado.Views;

import android.animation.ObjectAnimator;
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

    private TextView tabAll, tabGSM, tabAlerts;
    private View underline;
    private RecyclerView recyclerNotifications;

    private final List<String> allList = new ArrayList<>();
    private final List<String> gsmList = new ArrayList<>();
    private final List<String> alertList = new ArrayList<>();

    private SimpleNotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Tabs & underline
        tabAll = findViewById(R.id.tabAll);
        tabGSM = findViewById(R.id.tabGSM);
        tabAlerts = findViewById(R.id.tabAlerts);
        underline = findViewById(R.id.underline);

        // RecyclerView setup
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Load data
        loadLocalData();

        // Adapter
        adapter = new SimpleNotificationAdapter(new ArrayList<>(allList));
        recyclerNotifications.setAdapter(adapter);

        // Tabs click listeners
        tabAll.setOnClickListener(v -> switchTab("ALL", tabAll));
        tabGSM.setOnClickListener(v -> switchTab("GSM", tabGSM));
        tabAlerts.setOnClickListener(v -> switchTab("ALERTS", tabAlerts));

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_statics) {
                startActivity(new Intent(this, StaticsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Initial highlight
        highlightTab(tabAll);
    }

    // ✅ Données locales
    private void loadLocalData() {
        gsmList.clear();
        alertList.clear();
        allList.clear();

        gsmList.add("📶 GSM Sensor — Signal improved — 2m");
        gsmList.add("📡 GSM Module — Weak signal detected — 5m");
        gsmList.add("📡 GSM Antenna — Connection restored — 10m");

        alertList.add("⚠️ Alert System — High temperature detected! — 15m");
        alertList.add("⚠️ Alert System — Soil humidity too low! — 1h");

        allList.addAll(gsmList);
        allList.addAll(alertList);
    }

    // 🔄 Changement d’onglet
    private void switchTab(String type, TextView selectedTab) {
        highlightTab(selectedTab);

        List<String> newList = new ArrayList<>();

        switch (type) {
            case "ALL":
                newList.addAll(allList);
                animateUnderlineTo(tabAll);
                break;
            case "GSM":
                newList.addAll(gsmList);
                animateUnderlineTo(tabGSM);
                break;
            case "ALERTS":
                newList.addAll(alertList);
                animateUnderlineTo(tabAlerts);
                break;
        }

        adapter.updateList(newList);
    }

    // ✅ Animation underline centrée
    private void animateUnderlineTo(TextView targetTab) {
        underline.post(() -> {
            float tabCenterX = targetTab.getX() + (targetTab.getWidth() / 2f);
            float underlineTargetX = tabCenterX - (underline.getWidth() / 2f);

            ObjectAnimator animator = ObjectAnimator.ofFloat(underline, "translationX", underlineTargetX);
            animator.setDuration(250);
            animator.start();
        });
    }

    // ✅ Couleur des onglets
    private void highlightTab(TextView activeTab) {
        tabAll.setTextColor(getColor(R.color.navy_blue));
        tabGSM.setTextColor(getColor(R.color.navy_blue));
        tabAlerts.setTextColor(getColor(R.color.navy_blue));
        activeTab.setTextColor(getColor(R.color.gold));
    }

    // 🔹 Adapter local
    private static class SimpleNotificationAdapter extends RecyclerView.Adapter<SimpleNotificationAdapter.ViewHolder> {

        private final List<String> notifications;

        SimpleNotificationAdapter(List<String> notifications) {
            this.notifications = notifications;
        }

        void updateList(List<String> newList) {
            notifications.clear();
            notifications.addAll(newList);
            notifyDataSetChanged();
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

            // Split message / time
            String[] parts = notif.split("—");
            String message = parts.length > 0 ? parts[0].trim() : notif;
            String time = parts.length > 1 ? parts[parts.length - 1].trim() : "";

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
