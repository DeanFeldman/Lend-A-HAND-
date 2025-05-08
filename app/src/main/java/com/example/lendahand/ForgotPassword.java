package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import org.json.JSONObject;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPassword extends AppCompatActivity {
    EditText emailInput, newPasswordInput;
    Button resetButton;
    OkHttpClient client = new OkHttpClient();
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
        emailInput = findViewById(R.id.input_email);
        newPasswordInput = findViewById(R.id.input_new_password);
        resetButton = findViewById(R.id.button_reset_password);

        resetButton.setOnClickListener(v -> resetPassword());

        TextView textBackToLogin = findViewById(R.id.text_back_to_login);
        textBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
            startActivity(intent);
        });
    }

    public void resetPassword() {

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fname = prefs.getString("user_fname", "there");


        String email = emailInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();

        if (email.isEmpty() || newPassword.isEmpty()) {
            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Please enter your email and new password");
            return;
        }

        //validate new password
        if (newPassword.length() < 8) {
            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Password must be at least 8 characters long.");
            return;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSpecial = false;

        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }

        if (!hasUpper || !hasLower || !hasSpecial) {
            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Password must contain uppercase, lowercase, and special character.");
            return;
        }

        RequestBody formbody = new FormBody.Builder()
                .add("user_email",email)
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
                    try{
                        JSONObject json = new JSONObject(res);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");

                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, message);
                        if(success){
                            CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Password reset successful!");

                            //send email confirmation
                            new Thread(() -> {
                                EmailSender sender = new EmailSender();
                                sender.sendEmail(email, "Password Reset Confirmation", "Hi " + fname + ",\n\nYour password was successfully reset. If this wasn't you, please reply to this email.");
                            }).start();


                            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    catch(Exception e){
                        CUSTOMTOAST.showCustomToast(ForgotPassword.this, "Error: " + e.getMessage());
                    }
                });
            }
        });

    }

}

