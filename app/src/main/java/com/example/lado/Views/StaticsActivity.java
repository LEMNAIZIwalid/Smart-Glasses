package com.example.lado.Views;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statics);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statics);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                super.onPause();
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                super.onPause();
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_statics) {
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                super.onPause();
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                super.onPause();
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}
