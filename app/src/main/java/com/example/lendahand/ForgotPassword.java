package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPassword extends AppCompatActivity {

    enum Step {
        EMAIL, CODE, PASSWORD
    }

    EditText emailInput, verificationCodeInput, newPasswordInput;
    Button resetButton;
    LinearLayout stepEmail, stepCode, stepPassword;
    TextView textBackToLogin;
    Step currentStep = Step.EMAIL;

    OkHttpClient client = new OkHttpClient();
    String verificationCode ="";
    String enteredEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ForgotPassword), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        emailInput = findViewById(R.id.input_email);
        verificationCodeInput = findViewById(R.id.input_verification_code);
        newPasswordInput = findViewById(R.id.input_new_password);
        resetButton = findViewById(R.id.button_reset_password);

        stepEmail = findViewById(R.id.step_email);
        stepCode = findViewById(R.id.step_code);
        stepPassword = findViewById(R.id.step_password);

        textBackToLogin = findViewById(R.id.text_back_to_login);
        textBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
            startActivity(intent);
        });

        resetButton.setOnClickListener(v -> {
            switch (currentStep) {
                case EMAIL:
                    requestResetCode();
                    break;
                case CODE:
                    verifyCode();
                    break;
                case PASSWORD:
                    resetPassword();
                    break;
            }
        });

        showStep(Step.EMAIL);
    }

    private void showStep(Step step) {
        stepEmail.setVisibility(step == Step.EMAIL ? View.VISIBLE : View.GONE);
        stepCode.setVisibility(step == Step.CODE ? View.VISIBLE : View.GONE);
        stepPassword.setVisibility(step == Step.PASSWORD ? View.VISIBLE : View.GONE);
        currentStep = step;
    }

    private void requestResetCode() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            CUSTOMTOAST.showCustomToast(this, "Please enter your email.");
            return;
        }

        enteredEmail = email;

        RequestBody formBody = new FormBody.Builder()
                .add("user_email", email)
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/request_reset.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Network Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(res);
                        if (json.getBoolean("success")) {
                            String code = json.getString("code");
                            String fname = json.getString("first_name");

                            new Thread(() -> {
                                EmailSender sender = new EmailSender();
                                sender.sendEmail(
                                        email,
                                        "Password Reset Code",
                                        "Hi " + fname + ",\n\nYour password reset code is: " + code + "\n\nIt expires in 15 minutes."
                                );
                            }).start();


                            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Code sent to your email.");
                        } else {

                            CUSTOMTOAST.showCustomToast(ForgotPassword.this, json.getString("message"));
                        }
                    } catch (Exception e) {
                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Error: " + e.getMessage());
                    }
                });
            }
        });


        CUSTOMTOAST.showCustomToast(this, "Verification code sent to " + email);

        showStep(Step.CODE);
    }

    private void verifyCode() {
        String code = verificationCodeInput.getText().toString().trim();

        if (code.isEmpty()) {
            CUSTOMTOAST.showCustomToast(this, "Please enter the verification code.");
            return;
        }



        RequestBody requestBody = new FormBody.Builder()
                .add("user_email", enteredEmail)
                .add("code", code)
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/verify_code.php") // replace with your actual PHP URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(res);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, message);

                        if (success) {
                            showStep(Step.PASSWORD);
                        }
                    } catch (Exception e) {
                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Error: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void resetPassword() {
        String newPassword = newPasswordInput.getText().toString().trim();

        if (newPassword.isEmpty()) {
            CUSTOMTOAST.showCustomToast(this, "Please enter a new password.");
            return;
        }

        if (newPassword.length() < 8) {
            CUSTOMTOAST.showCustomToast(this, "Password must be at least 8 characters long.");
            return;
        }

        boolean hasUpper = false, hasLower = false, hasSpecial = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (!hasUpper || !hasLower || !hasSpecial) {
            CUSTOMTOAST.showCustomToast(this, "Password must contain uppercase, lowercase, and a special character.");
            return;
        }

        RequestBody formbody = new FormBody.Builder()
                .add("user_email", enteredEmail)
                .add("new_password", newPassword)
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/forgot_password.php")
                .post(formbody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Network Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(res);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, message);
                        if (success) {
                            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Password reset successful!");

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String fname = prefs.getString("user_fname", "there");

                            new Thread(() -> {
                                EmailSender sender = new EmailSender();
                                sender.sendEmail(enteredEmail,
                                        "Password Reset Confirmation",
                                        "Hi " + fname + ",\n\nYour password was successfully reset.\nIf this wasn't you, please reply to this email.");
                            }).start();

                            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Error: " + e.getMessage());
                    }
                });
            }
        });
    }
}
