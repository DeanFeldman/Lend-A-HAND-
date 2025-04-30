package com.example.lendahand;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button buttonLogin;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView textLogin = findViewById(R.id.here);
        textLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });


        buttonLogin = findViewById(R.id.button_login);
         emailInput = findViewById(R.id.input_email);
         passwordInput = findViewById(R.id.input_password);

        buttonLogin.setOnClickListener(view -> {
            processLogin();
        });
    }
        private void processLogin() {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }


                String url = "https://lamp.ms.wits.ac.za/home/s2698600/login.php";

                // Create request body
                RequestBody formBody = new FormBody.Builder()
                        .add("user_email", email)
                        .add("user_password", password)
                        .build();

                // Build the request
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();

                // Send the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String responseData = response.body().string();
                            try {
                                JSONObject json = new JSONObject(responseData);
                                boolean success = json.getBoolean("success");



                                runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                        JSONObject userObject = null;
                                        try {
                                            userObject = json.getJSONObject("user");
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                        int userId = 0;
                                        try {
                                            userId = userObject.getInt("user_id");
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }

                                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("user_email", email);
                                        editor.putInt("user_id", userId);
                                        editor.apply();


                                        Intent intent = new Intent(MainActivity.this, Donorwall.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String message = json.optString("message", "Login failed.");
                                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (Exception e) {
                                runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this, "Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                            }
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Server Error: " + response.message(), Toast.LENGTH_LONG).show()
                            );
                        }
                    }
                });
            }
        }



