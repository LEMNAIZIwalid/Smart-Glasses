package com.example.lado.Controller;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lado.Models.User;
import com.example.lado.Views.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SigninController {

    private Activity activity;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    public SigninController(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void creerCompte(EditText usernameField, EditText emailField, EditText phoneField, EditText passwordField) {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Création du compte dans Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // 🔹 Sauvegarde dans la base de données
                            User user = new User(username, email, phone, password);
                            usersRef.child(userId).setValue(user)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(activity, "Compte créé avec succès", Toast.LENGTH_SHORT).show();

                                            // 🔹 Redirection vers la page de connexion
                                            Intent intent = new Intent(activity, LoginActivity.class);
                                            activity.startActivity(intent);
                                            activity.finish();
                                        } else {
                                            Toast.makeText(activity, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(activity, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
