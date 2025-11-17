package com.example.lado.Controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lado.Views.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class LoginController {

    private final Activity activity;
    private final FirebaseAuth auth;
    private final DatabaseReference dbRef;

    public LoginController(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.dbRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void verifierLogin(EditText usernameField, EditText passwordField) {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String email = userSnap.child("email").getValue(String.class);
                                String storedPassword = userSnap.child("password").getValue(String.class);

                                if (storedPassword != null && storedPassword.equals(password) && email != null) {
                                    auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = auth.getCurrentUser();
                                                    if (user != null) {
                                                        SharedPreferences prefs = activity.getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE);
                                                        prefs.edit()
                                                                .putString("userId", user.getUid())
                                                                .putString("email", email)
                                                                .apply();

                                                        Toast.makeText(activity, "Connexion réussie ✅", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(activity, HomeActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        activity.startActivity(intent);
                                                        activity.finish();
                                                    }
                                                } else {
                                                    Toast.makeText(activity, "Erreur d’authentification Firebase", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    return;
                                }
                            }
                            Toast.makeText(activity, "Mot de passe incorrect ❌", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "Nom d’utilisateur introuvable ❌", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(activity, "Erreur DB : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
