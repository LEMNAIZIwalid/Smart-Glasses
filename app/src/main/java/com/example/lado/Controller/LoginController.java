package com.example.lado.Controller;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lado.Views.HomeActivity;

public class LoginController {

    private Activity activity;

    public LoginController(Activity activity) {
        this.activity = activity;
    }

    public void verifierLogin(EditText usernameField, EditText passwordField) {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.equals("lado2025") && password.equals("pass000")) {
            Intent intent = new Intent(activity, HomeActivity.class);
            activity.startActivity(intent);
            activity.finish(); // ferme la page login
        } else {
            Toast.makeText(activity, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}
