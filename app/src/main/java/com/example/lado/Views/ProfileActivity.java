package com.example.lado.Views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageProfile, btnChangePhoto;
    private Uri imageUri;
    private TextView textUsername;

    private Button btnModifyProfile, btnChangePassword, btnLogout;

    private DatabaseReference usersRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 🔹 Initialisation des vues
        imageProfile = findViewById(R.id.imageProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        textUsername = findViewById(R.id.textUsername); // TextView pour le username
        btnModifyProfile = findViewById(R.id.btnModifyProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        // 🔹 Référence DB corrigée (minuscule)
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 🔹 Récupérer UID depuis SharedPreferences
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", null);

        if (userId != null) {
            loadUsername(userId);
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }

        // 🔹 Charger l'image sauvegardée
        String savedUri = getSharedPreferences("PROFILE_PREF", MODE_PRIVATE)
                .getString("profile_image_uri", null);
        if (savedUri != null) {
            Glide.with(this)
                    .load(Uri.parse(savedUri))
                    .circleCrop()
                    .into(imageProfile);
        }

        // 🔹 Fallback : username local
        String savedUsername = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", null);
        if (savedUsername != null) {
            textUsername.setText(savedUsername);
        }

        // 🔹 Modifier la photo
        btnChangePhoto.setOnClickListener(v -> openImageChooser());

        // 🔹 Aller vers ModifyProfileActivity
        btnModifyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ModifyProfileActivity.class);
            startActivity(intent);
        });

        // 🔹 Aller vers ResetPasswordActivity
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // 🔹 Pop-up déconnexion
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // 🔹 Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historique) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0,0); finish(); return true;
            } else if (id == R.id.nav_livestream) {
                startActivity(new Intent(this, LivestreamActivity.class));
                overridePendingTransition(0,0); finish(); return true;
            } else if (id == R.id.nav_statics) {
                startActivity(new Intent(this, StaticsActivity.class));
                overridePendingTransition(0,0); finish(); return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0,0); finish(); return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // 🔹 Charger le username depuis Firebase
    private void loadUsername(String uid) {
        usersRef.child(uid).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String username = snapshot.getValue(String.class);
                        if (username != null) {
                            textUsername.setText(username);

                            // 🔹 Sauvegarde locale pour affichage rapide
                            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("username", username)
                                    .apply();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Erreur DB : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 Sélecteur d'image
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choisir une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Glide.with(this).load(imageUri).circleCrop().into(imageProfile);
                getSharedPreferences("PROFILE_PREF", MODE_PRIVATE)
                        .edit()
                        .putString("profile_image_uri", imageUri.toString())
                        .apply();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l’image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
