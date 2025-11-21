package com.example.lado.Views;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lado.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword, editTextConfirmPassword;
    private ImageView iconToggleNewPassword, iconToggleConfirmPassword;
    private Button buttonConfirmReset;

    private ImageView iconLength, iconUpper, iconLower, iconNumber, iconSymbol;
    private TextView textRuleLength, textRuleUpper, textRuleLower, textRuleNumber, textRuleSymbol;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth auth;
    private String oobCode; // code de réinitialisation envoyé par email (optionnel)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        oobCode = getIntent().getStringExtra("oobCode"); // présent si réinitialisation via email

        // 🔹 Initialisation vues
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        iconToggleNewPassword = findViewById(R.id.iconToggleNewPassword);
        iconToggleConfirmPassword = findViewById(R.id.iconToggleConfirmPassword);
        buttonConfirmReset = findViewById(R.id.buttonConfirmReset);

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

        // 🔹 Toggle visibilité mot de passe
        iconToggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextNewPassword, iconToggleNewPassword, true));
        iconToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextConfirmPassword, iconToggleConfirmPassword, false));

        // 🔹 Mise à jour dynamique des règles mot de passe
        editTextNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRules(s.toString());
            }
        });

        // 🔹 Bouton confirmation
        buttonConfirmReset.setOnClickListener(v -> resetPassword());
    }

    private void updatePasswordRules(String password){
        setRuleState(password.length() >= 8, iconLength, textRuleLength);
        setRuleState(Pattern.compile(".*[A-Z].*").matcher(password).find(), iconUpper, textRuleUpper);
        setRuleState(Pattern.compile(".*[a-z].*").matcher(password).find(), iconLower, textRuleLower);
        setRuleState(Pattern.compile(".*[0-9].*").matcher(password).find(), iconNumber, textRuleNumber);
        setRuleState(Pattern.compile(".*[@.,;_].*").matcher(password).find(), iconSymbol, textRuleSymbol);
    }

    private void setRuleState(boolean valid, ImageView icon, TextView text){
        if(valid){
            icon.setImageResource(R.drawable.ic_check_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            icon.setImageResource(R.drawable.ic_redcross_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void resetPassword(){
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if(newPassword.isEmpty() || confirmPassword.isEmpty()){
            showToast("Veuillez remplir tous les champs");
            return;
        }

        if(!newPassword.equals(confirmPassword)){
            showToast("Les mots de passe ne correspondent pas");
            return;
        }

        if(!isValidPassword(newPassword)){
            showToast("Le mot de passe ne respecte pas les règles");
            return;
        }

        // 🔹 Cas 1 : Réinitialisation via email (oobCode présent)
        if(oobCode != null){
            auth.confirmPasswordReset(oobCode, newPassword)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            showToast("Mot de passe réinitialisé ✅");
                            finish();
                        } else {
                            showToast("Erreur reset : " + task.getException().getMessage());
                        }
                    });
        }
        // 🔹 Cas 2 : Utilisateur connecté souhaite changer son mot de passe
        else {
            FirebaseUser user = auth.getCurrentUser();
            if(user != null){
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                showToast("Mot de passe modifié ✅");
                                finish();
                            } else {
                                showToast("Erreur modification : " + task.getException().getMessage());
                            }
                        });
            } else {
                showToast("Utilisateur non connecté, impossible de changer le mot de passe");
            }
        }
    }

    private boolean isValidPassword(String password){
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@.,;_]).{8,}$";
        return Pattern.compile(pattern).matcher(password).matches();
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isNewField){
        if(isNewField){
            isNewPasswordVisible = !isNewPasswordVisible;
            editText.setInputType(isNewPasswordVisible ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            icon.setImageResource(isNewPasswordVisible ? R.drawable.ic_visibilityoff_30 : R.drawable.ic_visibility_30);
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            editText.setInputType(isConfirmPasswordVisible ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            icon.setImageResource(isConfirmPasswordVisible ? R.drawable.ic_visibilityoff_30 : R.drawable.ic_visibility_30);
        }
        editText.setSelection(editText.getText().length());
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
