package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
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

public class Profile extends AppCompatActivity {
        private EditText editTextName;
        private EditText editTextBio;
        private TextView displayEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextName = findViewById(R.id.input_name);
        editTextBio  = findViewById(R.id.input_bio);
        displayEmail = findViewById(R.id.display_email);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        displayEmail.setText(email);

        Button buttonDonorWall = findViewById(R.id.button_DonorWall);
        buttonDonorWall.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Donorwall.class);
            startActivity(intent);
        });

        Button buttonDonate = findViewById(R.id.button_Donate);
        buttonDonate.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Donate.class);
            startActivity(intent);
        });

        Button buttonRecieve = findViewById(R.id.button_Recieve);
        buttonRecieve.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Recieve.class);
            startActivity(intent);
        });

        CUSTOMTOAST.showCustomToast(this, "Click on the fields you want to change.");

        Button buttonLogin = findViewById(R.id.button_save_profile);

        buttonLogin.setOnClickListener(view -> {
            String fullName = editTextName.getText().toString().trim();
            String[] nameParts = fullName.split(" ", 2);

            String fname = "";
            String lname = "";
            String bio = editTextBio.getText().toString().trim();

            if (nameParts.length == 1) {
                fname = nameParts[0];
                lname = "";
            } else if (nameParts.length == 2) {
                fname = nameParts[0];
                lname = nameParts[1];
            }

            if (!email.isEmpty()) {
                updateUserInDatabase(email, fname, lname, bio);
            } else {
                CUSTOMTOAST.showCustomToast(Profile.this, "No email saved. Cannot update profile.");
            }

        });


        Button logoutButton = findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> {

            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(Profile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fetchUserFromDatabase();
    }

    private void fetchUserFromDatabase() {

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");

        if (email.isEmpty()) {
            CUSTOMTOAST.showCustomToast(this, "No email saved. Cannot fetch profile.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("user_email", email)
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/getuser.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getBoolean("success")) {
                            String name = obj.getString("full_name");
                            String bio = obj.getString("biography");

                            runOnUiThread(() -> {
                                editTextName.setText(name);
                                editTextBio.setText(bio);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    private void updateUserInDatabase(String email, String fname, String lname, String bio) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("user_email", email)
                .add("user_fname", fname)
                .add("user_lname", lname)
                .add("user_biography", bio)
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/updateuser.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Profile.this, "Update failed.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(json);
                        if (obj.getBoolean("success")) {
                            runOnUiThread(() ->
                                    CUSTOMTOAST.showCustomToast(Profile.this, "Profile updated!")
                            );
                        } else {
                            String msg = obj.getString("message");
                            runOnUiThread(() ->
                                    CUSTOMTOAST.showCustomToast(Profile.this, "Update error: " + msg)
                            );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(Profile.this, "JSON error!", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }

        });
    }
}