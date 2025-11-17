package com.example.lado.Views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lado.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword, editTextConfirmPassword;
    private ImageView iconToggleNewPassword, iconToggleConfirmPassword;
    private Button buttonConfirmReset;

    // Password rule icons
    private ImageView iconLength, iconUpper, iconLower, iconNumber, iconSymbol;
    private TextView textRuleLength, textRuleUpper, textRuleLower, textRuleNumber, textRuleSymbol;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // 🔹 Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Error: No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Récupération du userId depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        // 🔹 Référence Realtime Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 🔹 Initialisation vues
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        iconToggleNewPassword = findViewById(R.id.iconToggleNewPassword);
        iconToggleConfirmPassword = findViewById(R.id.iconToggleConfirmPassword);
        buttonConfirmReset = findViewById(R.id.buttonConfirmReset);

        // Rule components
        iconLength = findViewById(R.id.iconLength);
        iconUpper = findViewById(R.id.iconUpper);
        iconLower = findViewById(R.id.iconLower);
        iconNumber = findViewById(R.id.iconNumber);
        iconSymbol = findViewById(R.id.iconSymbol);

        textRuleLength = findViewById(R.id.textRuleLength);
        textRuleUpper = findViewById(R.id.textRuleUpper);
        textRuleLower = findViewById(R.id.textRuleLower);
        textRuleNumber = findViewById(R.id.textRuleNumber);
        textRuleSymbol = findViewById(R.id.textRuleSymbol);

        // 🔹 Toggle visibilité
        iconToggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextNewPassword, iconToggleNewPassword, true));

        iconToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextConfirmPassword, iconToggleConfirmPassword, false));

        // 🔹 Règles mot de passe en temps réel
        editTextNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRules(s.toString());
            }
        });

        // 🔹 Bouton confirmation
        buttonConfirmReset.setOnClickListener(v -> validateAndChangePassword());
    }

    private void updatePasswordRules(String password) {
        setRuleState(password.length() >= 8, iconLength, textRuleLength);
        setRuleState(Pattern.compile(".*[A-Z].*").matcher(password).find(), iconUpper, textRuleUpper);
        setRuleState(Pattern.compile(".*[a-z].*").matcher(password).find(), iconLower, textRuleLower);
        setRuleState(Pattern.compile(".*[0-9].*").matcher(password).find(), iconNumber, textRuleNumber);
        setRuleState(Pattern.compile(".*[@.,;_].*").matcher(password).find(), iconSymbol, textRuleSymbol);
    }

    private void setRuleState(boolean valid, ImageView icon, TextView text) {
        if (valid) {
            icon.setImageResource(R.drawable.ic_check_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            icon.setImageResource(R.drawable.ic_redcross_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void validateAndChangePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        if (!isValidPassword(newPassword)) {
            showToast("Password does not meet requirements");
            return;
        }

        // 🔹 Mise à jour mot de passe dans Firebase Auth
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 🔹 Mise à jour mot de passe dans Realtime Database
                        if (userId != null) {
                            usersRef.child(userId).child("password").setValue(newPassword)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            showToast("Password changed successfully in Auth and Database!");
                                            finish();
                                        } else {
                                            showToast("Password updated in Auth but failed in Database: " + dbTask.getException().getMessage());
                                        }
                                    });
                        } else {
                            showToast("User ID not found. Password changed in Auth only.");
                            finish();
                        }
                    } else {
                        showToast("Error updating password in Auth: " + task.getException().getMessage());
                    }
                });
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@.,;_]).{8,}$";
        return Pattern.compile(pattern).matcher(password).matches();
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isNewField) {
        if (isNewField) {
            isNewPasswordVisible = !isNewPasswordVisible;
            editText.setInputType(isNewPasswordVisible ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            icon.setImageResource(isNewPasswordVisible ?
                    R.drawable.ic_visibilityoff_30 :
                    R.drawable.ic_visibility_30
            );
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            editText.setInputType(isConfirmPasswordVisible ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            icon.setImageResource(isConfirmPasswordVisible ?
                    R.drawable.ic_visibilityoff_30 :
                    R.drawable.ic_visibility_30
            );
        }
        editText.setSelection(editText.getText().length());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
