package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lado.Controller.SigninController;
import com.example.lado.R;

public class SignInActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone, editPassword, editConfirmPassword;
    private Button btnSignUp;
    private TextView textGoLogin;

    private SigninController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // ----- Liaison UI -----
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        textGoLogin = findViewById(R.id.textGoLogin);

        controller = new SigninController(this);

        // ----- Aller vers Login -----
        textGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        // ----- Création du compte -----
        btnSignUp.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            controller.creerCompte(username, email, phone, password, confirmPassword);
        });
    }
}
