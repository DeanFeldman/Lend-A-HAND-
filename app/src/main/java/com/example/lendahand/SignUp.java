package com.example.lendahand;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;

import android.text.TextWatcher;

import java.util.Locale;

import android.view.WindowManager;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
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
    Button btnSignUp;
    EditText txtFName , txtLName , txtEmail, txtPassword,dtpDOB,txtrepeatpassword;

    TextView txtLogin, txtPasswordLength, txtPasswordUpper, txtPasswordLower, txtPasswordSpecial;

    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        txtrepeatpassword = findViewById(R.id.input_confirm_password);
        btnSignUp = findViewById(R.id.button_signup);
        dtpDOB = findViewById(R.id.input_dob);
        txtPasswordLength = findViewById(R.id.password_length);
        txtPasswordUpper = findViewById(R.id.password_upper);
        txtPasswordLower = findViewById(R.id.password_lower);
        txtPasswordSpecial = findViewById(R.id.password_special);


        btnSignUp.setOnClickListener(view -> {
            processSignUp();
        });

        txtLogin = findViewById(R.id.text_login);
        txtLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        dtpDOB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        dtpDOB.setText(date);
                    },
                    year, month, day
            );


            try {
                datePickerDialog.getDatePicker().setCalendarViewShown(false);
            } catch (Exception ignored) {
            }

            datePickerDialog.show();
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwd = s.toString();


                if (pwd.length() >= 8) {
                    txtPasswordLength.setText(" ✅ 8+ characters");
                } else {
                    txtPasswordLength.setText(" ❌ 8+ characters");
                }

                boolean hasUpper = false, hasLower = false;
                for (char c : pwd.toCharArray()) {
                    if (Character.isUpperCase(c)) hasUpper = true;
                    if (Character.isLowerCase(c)) hasLower = true;
                }

                if (hasUpper) {
                    txtPasswordUpper.setText(" ✅ Uppercase letter");
                } else {
                    txtPasswordUpper.setText(" ❌ Uppercase letter");
                }

                if (hasLower) {
                    txtPasswordLower.setText( " ✅ Lowercase letter");
                } else {
                    txtPasswordLower.setText(" ❌ Lowercase letter");
                }

                boolean hasSpecial = false;
                for (char c : pwd.toCharArray()) {
                    if (!Character.isLetterOrDigit(c)) {
                        hasSpecial = true;
                        break;
                    }
                }

                if (hasSpecial) {
                    txtPasswordSpecial.setText(" ✅ Special character (!@#...)");
                } else {
                    txtPasswordSpecial.setText(" ❌ Special character (!@#...)");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void processSignUp() {
        String fname = txtFName.getText().toString().trim();
        String lname = txtLName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String dob = dtpDOB.getText().toString().trim();

        //checks if fields are empty
        if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUp.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] parts = dob.split("/");

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int year = Integer.parseInt(parts[2]);

        if (TextUtils.isEmpty(dob)) {
            CUSTOMTOAST.showCustomToast(this, "Please select your Date of Birth");
            return;
        }

        if(txtEmail.getText().toString().contains("@")){
            CUSTOMTOAST.showCustomToast(this, "Please enter a valid email address");
            return;
        }

        String formattedDob = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);

        //check age is > 18 years old
        Calendar today = Calendar.getInstance();
        Calendar dobCalendar = Calendar.getInstance();
        dobCalendar.set(year, month, day);

        //check if bday in future
        if (dobCalendar.after(today)) {
            CUSTOMTOAST.showCustomToast(this, "Date of Birth cannot be in the future");
            return;
        }
        //check if bday has occured this yr alr
        Calendar birthdayThisYear = (Calendar) dobCalendar.clone();
        birthdayThisYear.set(Calendar.YEAR, today.get(Calendar.YEAR));

        int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
        if (today.before(birthdayThisYear)) {
            age--;
        }

        if (age < 18) {
            CUSTOMTOAST.showCustomToast(this, "You must be at least 18 years old");
            return;
        }

        //make sure the passwords match
        if(!txtPassword.getText().toString().equals(txtrepeatpassword.getText().toString())){
            CUSTOMTOAST.showCustomToast(SignUp.this, "Passwords do not match.");
            return;
        }

        //password >=8 chars
        if (password.length() <= 8) {
            CUSTOMTOAST.showCustomToast(SignUp.this, "Password must be at least 8 characters long.");
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
                .add("user_dob", formattedDob)
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

                                //send email confirmation
                                new Thread(() -> {
                                    EmailSender sender = new EmailSender();
                                    sender.sendEmail(email, "Welcome to LendAHand!", "Hi " + fname + ",\n\nThanks for signing up for LendAHand!");
                                }).start();

                                runOnUiThread(() -> CUSTOMTOAST.showCustomToast(SignUp.this, "Email Confirmation sent!"));

                                //swap pages
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