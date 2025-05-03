package com.example.lendahand;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {
    Button buttonSignUp;
    EditText txtFName , txtLName , txtEmail, txtPassword, txtDOB;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtFName = findViewById(R.id.input_first_name);
        txtLName = findViewById(R.id.input_surname);
        txtEmail = findViewById(R.id.input_email);
        txtPassword = findViewById(R.id.input_password);
        buttonSignUp = findViewById(R.id.button_signup);
        txtDOB=findViewById(R.id.input_dob);
        txtDOB.setOnClickListener(view -> showDatePicker());

        buttonSignUp.setOnClickListener(view -> {
           processSignUp();
        });

    }
    private void showDatePicker() {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignUp.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String dob = selectedYear + "-" + String.format("%02d", selectedMonth + 1) + "-" + String.format("%02d", selectedDay);
                    txtDOB.setText(dob);
                },
                year, month, day);

        datePickerDialog.show();
    }
    private void processSignUp() {
        String fname = txtFName.getText().toString().trim();
        String lname = txtLName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String dob = txtDOB.getText().toString().trim();


        if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUp.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(SignUp.this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (!hasUpper || !hasLower || !hasSpecial) {
            Toast.makeText(SignUp.this, "Password must contain uppercase, lowercase, and special character.", Toast.LENGTH_SHORT).show();
            return;
        }


        String url = "https://lamp.ms.wits.ac.za/home/s2698600/signup.php";

        RequestBody formBody = new FormBody.Builder()
                .add("user_fname", fname)
                .add("user_lname", lname)
                .add("user_dob", dob)
                .add("user_email", email)
                .add("user_password", password)
                .add("user_biography", "")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SignUp.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
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
                                Toast.makeText(SignUp.this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = json.optString("message", "Signup failed.");
                                Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(SignUp.this, "Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(SignUp.this, "Server Error: " + response.message(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}