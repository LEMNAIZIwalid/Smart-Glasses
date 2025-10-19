package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Sélection initiale sur Home
        bottomNavigationView.setSelectedItemId(R.id.nav_historique);

        // Pour l'instant, navBottom visible mais désactivé (aucune action)
        bottomNavigationView.setOnItemSelectedListener(item -> false);
    }
}
