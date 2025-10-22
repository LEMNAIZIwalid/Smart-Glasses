package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lado.Controller.LoginController;
import com.example.lado.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView textSignIn, textForgot; // 🔹 Lien vers Sign In & Forgot Password
    private LoginController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔸 Liaison des vues XML
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textSignIn = findViewById(R.id.textSignIn);
        textForgot = findViewById(R.id.textForgot); // 🔹 Nouveau lien "Forgot password?"

        // 🔸 Initialisation du contrôleur
        controller = new LoginController(this);

        // 🟢 Action sur le bouton "Login"
        btnLogin.setOnClickListener(v -> controller.verifierLogin(editUsername, editPassword));

        // 🟡 Action sur le texte "Sign In"
        textSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // 🔴 Action sur le texte "Forgot password?"
        textForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, VerifyUserActivity.class);
            startActivity(intent);
        });
    }
}
