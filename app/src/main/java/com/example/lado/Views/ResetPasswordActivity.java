package com.example.lado.Views;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lado.R;

import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword, editTextConfirmPassword;
    private ImageView iconToggleNewPassword, iconToggleConfirmPassword;
    private Button buttonConfirmReset;

    // ✅ Rule icons & texts
    private ImageView iconLength, iconUpper, iconLower, iconNumber, iconSymbol;
    private TextView textRuleLength, textRuleUpper, textRuleLower, textRuleNumber, textRuleSymbol;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // 🔹 Inputs
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        iconToggleNewPassword = findViewById(R.id.iconToggleNewPassword);
        iconToggleConfirmPassword = findViewById(R.id.iconToggleConfirmPassword);
        buttonConfirmReset = findViewById(R.id.buttonConfirmReset);

        // 🔹 Rules views
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

        // 👁 Toggle visibility listeners
        iconToggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextNewPassword, iconToggleNewPassword, true));
        iconToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(editTextConfirmPassword, iconToggleConfirmPassword, false));

        // 🧠 Listen password typing
        editTextNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRules(s.toString());
            }
        });

        // ✅ Confirm button click
        buttonConfirmReset.setOnClickListener(v -> validatePasswords());
    }

    // 🧩 Update checklist rules live
    private void updatePasswordRules(String password) {
        // 8+ characters
        setRuleState(password.length() >= 8, iconLength, textRuleLength);

        // Uppercase
        setRuleState(Pattern.compile(".*[A-Z].*").matcher(password).find(), iconUpper, textRuleUpper);

        // Lowercase
        setRuleState(Pattern.compile(".*[a-z].*").matcher(password).find(), iconLower, textRuleLower);

        // Number
        setRuleState(Pattern.compile(".*[0-9].*").matcher(password).find(), iconNumber, textRuleNumber);

        // Special symbol
        setRuleState(Pattern.compile(".*[@.,;_].*").matcher(password).find(), iconSymbol, textRuleSymbol);
    }

    // 🟢 Change rule color + icon dynamically
    private void setRuleState(boolean valid, ImageView icon, TextView text) {
        if (valid) {
            icon.setImageResource(R.drawable.ic_check_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            icon.setImageResource(R.drawable.ic_redcross_48);
            text.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    // ✅ Validation on submit
    private void validatePasswords() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields");
            return;
        }

        if (!isValidPassword(newPassword)) {
            showToast("Password must meet all requirements");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        showToast("Password successfully reset!");
        finish();
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@.,;_]).{8,}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isNew) {
        if (isNew) {
            isNewPasswordVisible = !isNewPasswordVisible;
            editText.setInputType(isNewPasswordVisible
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(isNewPasswordVisible
                    ? R.drawable.ic_visibilityoff_30
                    : R.drawable.ic_visibility_30);
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            editText.setInputType(isConfirmPasswordVisible
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(isConfirmPasswordVisible
                    ? R.drawable.ic_visibilityoff_30
                    : R.drawable.ic_visibility_30);
        }
        editText.setSelection(editText.getText().length());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
