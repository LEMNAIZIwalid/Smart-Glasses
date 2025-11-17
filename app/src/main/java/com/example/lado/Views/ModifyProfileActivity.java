package com.example.lado.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lado.Models.User;
import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

public class ModifyProfileActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone;
    private Button btnSaveChanges;
    private DatabaseReference usersRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);

        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        usersRef = FirebaseDatabase.getInstance().getReference("users"); // ✅ minuscule

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        if (userId != null) loadUserData(userId);
        else Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();

        btnSaveChanges.setOnClickListener(v -> updateUserData());

        setupBottomNav();
    }

    private void loadUserData(String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        editUsername.setText(user.getUsername());
                        editEmail.setText(user.getEmail());
                        editPhone.setText(user.getPhone());
                    }
                } else {
                    Toast.makeText(ModifyProfileActivity.this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ModifyProfileActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        if (userId == null) return;

        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userId).child("username").setValue(username);
        usersRef.child(userId).child("email").setValue(email);
        usersRef.child(userId).child("phone").setValue(phone)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profil mis à jour avec succès ✅", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_historique)
                startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_livestream)
                startActivity(new Intent(this, LivestreamActivity.class));
            else if (id == R.id.nav_statics)
                startActivity(new Intent(this, StaticsActivity.class));
            else if (id == R.id.nav_notifications)
                startActivity(new Intent(this, NotificationsActivity.class));
            else if (id == R.id.nav_profile)
                return true;

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }
}
