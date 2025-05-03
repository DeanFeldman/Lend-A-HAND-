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
    EditText txtFName , txtLName , txtEmail, txtPassword;
    DatePicker dtpDOB;
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
        dtpDOB = findViewById(R.id.input_dob);

        buttonSignUp.setOnClickListener(view -> {
           processSignUp();
        });

    }

    private void processSignUp() {
        String fname = txtFName.getText().toString().trim();
        String lname = txtLName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        int day = dtpDOB.getDayOfMonth();
        int month = dtpDOB.getMonth();
        int year = dtpDOB.getYear();

        String dob = String.format("%04d-%02d-%02d", year, month + 1, day);

        //check age is > 18 years old
        Calendar calendarDOB = Calendar.getInstance();
        calendarDOB.set(year, month, day);
        Calendar calendarToday = Calendar.getInstance();
        int age = calendarToday.get(Calendar.YEAR) - calendarDOB.get(Calendar.YEAR);

        //adjusts if the person hasnt had their birthday yet
        if (calendarToday.get(Calendar.DAY_OF_YEAR) < calendarDOB.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        if(age < 18){
            CUSTOMTOAST.showCustomToast(this, "You must be atleast 18 years old");
            return;
        }
        if(calendarDOB.after(calendarToday)){
            CUSTOMTOAST.showCustomToast(this, "Date of Birth Cannot be in the future");
            return;
        }

        //checks empty
        if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUp.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        //password >6 chars
        if (password.length() < 6) {
            CUSTOMTOAST.showCustomToast(SignUp.this, "Password must be at least 6 characters long.");
            return;
        }
        //checks the password contains uppercase, lowercase and special character
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)){
                hasUpper = true;
            }
            else if (Character.isLowerCase(c)){
                hasLower = true;
            }
            else if (!Character.isLetterOrDigit(c)){
                hasSpecial = true;
            }
        }

        if (!hasUpper || !hasLower || !hasSpecial) {
            CUSTOMTOAST.showCustomToast(SignUp.this, "Password must contain uppercase, lowercase, and special character.");
            return;
        }

        //send new user to db
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
                        CUSTOMTOAST.showCustomToast(SignUp.this, "Network Error: " + e.getMessage())
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
                                CUSTOMTOAST.showCustomToast(SignUp.this, "Signup successful! Please login.");

                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = json.optString("message", "Signup failed.");
                                CUSTOMTOAST.showCustomToast(SignUp.this, message);
                            }
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                CUSTOMTOAST.showCustomToast(SignUp.this, "Parsing Error: " + e.getMessage())
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            CUSTOMTOAST.showCustomToast(SignUp.this, "Server Error: " + response.message())
                    );
                }
            }
        });
    }
}