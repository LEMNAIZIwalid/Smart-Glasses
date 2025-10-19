package com.example.lado.Views;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                super.onPause();
                finish();
                return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                overridePendingTransition(0, 0);
                super.onPause();
                finish();
                return true;
            } else if (id == R.id.nav_statics) {
                startActivity(new Intent(this, StaticsActivity.class));
                overridePendingTransition(0, 0);
                super.onPause();
                finish();
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                super.onPause();
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
