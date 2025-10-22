package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lado.R;

public class VerifyUserActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPhone;
    private Button buttonVerify, buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonVerify = findViewById(R.id.buttonVerify);
        buttonCancel = findViewById(R.id.buttonCancel);

        // 🟢 Vérification (exemple simple à adapter avec ton backend)
        buttonVerify.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Vérifie via ton serveur RMI / Spring Boot
            if (email.equals("test@gmail.com") && phone.equals("0612345678")) {
                Toast.makeText(this, "Vérification réussie", Toast.LENGTH_SHORT).show();

                // Redirige vers la page pour changer le mot de passe
                Intent intent = new Intent(VerifyUserActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Email ou numéro incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔴 Annuler = Retour au login
        buttonCancel.setOnClickListener(v -> finish());
    }
}
