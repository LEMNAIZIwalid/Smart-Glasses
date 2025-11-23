package com.example.lado.Controller;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.lado.Models.User;
import com.example.lado.Views.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SigninController {

    private Activity activity;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private final String DEFAULT_PROFILE_URL =
            "https://firebasestorage.googleapis.com/v0/b/your-app.appspot.com/o/default_profile.png?alt=media";

    private final String DEFAULT_OBSTACLE_IMAGE =
            "https://firebasestorage.googleapis.com/v0/b/your-app.appspot.com/o/no_image.png?alt=media";

    public SigninController(Activity activity) {
        this.activity = activity;
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void creerCompte(String username, String email, String phone,
                            String password, String confirmPassword) {

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(activity, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(activity, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();

                        // 🔹 Créer User principal
                        User user = new User(username, email, phone, DEFAULT_PROFILE_URL);
                        usersRef.child(userId).setValue(user);

                        // 🔹 Créer sensors
                        Map<String, Object> camera = new HashMap<>();
                        camera.put("status", "on");
                        camera.put("obstacle_image", DEFAULT_OBSTACLE_IMAGE);

                        Map<String, Object> current = new HashMap<>();
                        current.put("distance", 0);
                        current.put("stamp", System.currentTimeMillis());
                        current.put("status", "off");

                        Map<String, Object> ultrasonic = new HashMap<>();
                        ultrasonic.put("distance", 0);

                        Map<String, Object> sensors = new HashMap<>();
                        sensors.put("camera", camera);
                        sensors.put("current", current);
                        sensors.put("ultrasonic", ultrasonic);

                        usersRef.child(userId).child("sensors").setValue(sensors);

                        // 🔹 Notifications vides
                        Map<String, Object> notifications = new HashMap<>();
                        notifications.put("camera", new HashMap<String, Object>());
                        notifications.put("ultrasonic", new HashMap<String, Object>());
                        usersRef.child(userId).child("notifications").setValue(notifications);

                        Toast.makeText(activity, "Compte créé avec succès ✅", Toast.LENGTH_SHORT).show();

                        // 🔹 Redirection
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    } else {
                        Toast.makeText(activity,
                                "Erreur Auth : " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
