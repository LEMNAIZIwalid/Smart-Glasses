package com.example.lado.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.lado.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerifyUserActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPhone;
    private Button buttonVerify, buttonCancel;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_user);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonVerify = findViewById(R.id.buttonVerify);
        buttonCancel = findViewById(R.id.buttonCancel);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

        buttonVerify.setOnClickListener(v -> verifyUserAndSendResetEmail());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void verifyUserAndSendResetEmail() {
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if(email.isEmpty() || phone.isEmpty()){
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            boolean found = false;
                            for(DataSnapshot userSnap : snapshot.getChildren()){
                                String dbPhone = userSnap.child("phone").getValue(String.class);
                                if(dbPhone != null && dbPhone.equals(phone)){
                                    found = true;
                                    // 🔹 Envoi email reset password
                                    auth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener(task -> {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(VerifyUserActivity.this,
                                                            "Un email de réinitialisation a été envoyé ✅",
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(VerifyUserActivity.this,
                                                            "Erreur : " + task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    break;
                                }
                            }
                            if(!found){
                                Toast.makeText(VerifyUserActivity.this, "Numéro incorrect ❌", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VerifyUserActivity.this, "Email introuvable ❌", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(VerifyUserActivity.this, "Erreur DB : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
