package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
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
        private EditText txtName, txtBio;
        private TextView txtEmail, txtDonatedSummary, txtReceivedSummary, txtDonorContacts, txtOutstandingSummary;


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

        txtName = findViewById(R.id.input_name);
        txtBio = findViewById(R.id.input_bio);
        txtEmail = findViewById(R.id.display_email);
        txtDonatedSummary = findViewById(R.id.text_donated_summary);
        txtReceivedSummary = findViewById(R.id.text_received_summary);
        txtDonorContacts = findViewById(R.id.text_donor_contacts);
        txtOutstandingSummary = findViewById(R.id.text_outstanding_summary);


        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        int user_Id = prefs.getInt("user_id",-1);

        if(user_Id != -1){
            fetchUserStats(user_Id, txtDonatedSummary, txtReceivedSummary, txtOutstandingSummary);
        }
        txtEmail.setText(email);


        Button buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Donorwall.class);
            startActivity(intent);
        });


        CUSTOMTOAST.showCustomToast(this, "Click on the fields you want to change.");

        Button buttonLogin = findViewById(R.id.button_save_profile);

        buttonLogin.setOnClickListener(view -> {
            String fullName = txtName.getText().toString().trim();
            String[] nameParts = fullName.split(" ", 2);

            String fname = "";
            String lname = "";
            String bio = txtBio.getText().toString().trim();

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


        fetchDonorContacts(user_Id, txtDonorContacts);
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
                                txtName.setText(name);
                                txtBio.setText(bio);
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

    private void fetchUserStats(int userId, TextView txtDonatedSummary, TextView txtReceivedSummary, TextView txtOutstandingSummary) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/get_user_stats.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    txtDonatedSummary.setText("Failed to load donations.");
                    txtReceivedSummary.setText("Failed to load received items.");
                    txtOutstandingSummary.setText("Failed to load received items.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        txtDonatedSummary.setText("Error loading donations.");
                        txtReceivedSummary.setText("Error loading received items.");
                        txtOutstandingSummary.setText("Error loading received items.");
                    });
                    return;
                }

                String jsonData = response.body().string();

                try{
                    JSONObject json = new JSONObject(jsonData);
                    Log.e("PROFILE_JSON", jsonData);

                    JSONArray donatedArray = json.getJSONArray("donated");
                    JSONArray receivedArray = json.getJSONArray("received");

                    StringBuilder donatedText = new StringBuilder();
                    StringBuilder receivedText = new StringBuilder();

                    if (donatedArray.length() == 0) {
                        donatedText.append("You have not donated any items.");
                    }
                    else{
                        donatedText.append("You have donated:\n");
                        for (int i = 0; i < donatedArray.length(); i++) {
                            JSONObject item = donatedArray.getJSONObject(i);
                            String name = item.getString("item_name");
                            int qty = item.getInt("total_donated");
                            donatedText.append("•  ").append(name).append(": ").append(qty).append("\n");
                        }
                    }

                    if(receivedArray.length() == 0){
                        receivedText.append("You have not received any items.");
                    }
                    else{
                        receivedText.append("You have received:\n");
                        for (int i = 0; i < receivedArray.length(); i++) {
                            JSONObject item = receivedArray.getJSONObject(i);
                            String name = item.getString("item_name");
                            int qty = item.getInt("total_received");
                            receivedText.append("•  ").append(name).append(": ").append(qty).append("\n");
                        }
                    }
                    JSONArray outstandingArray = json.getJSONArray("outstanding");
                    StringBuilder outstandingText = new StringBuilder();

                    if (outstandingArray.length() == 0) {
                        outstandingText.append("You have no unfulfilled requests.");
                    } else {
                        outstandingText.append("Your unfulfilled requests:\n");
                        for (int i = 0; i < outstandingArray.length(); i++) {
                            JSONObject item = outstandingArray.getJSONObject(i);
                            String name = item.getString("item_name");
                            int outstandingQty = item.getInt("outstanding_quantity");
                            outstandingText.append("•  ").append(name)
                                    .append(": ").append(outstandingQty)
                                    .append(" still needed\n");

                        }
                    }


                    runOnUiThread(() -> {
                        txtDonatedSummary.setText(donatedText.toString().trim());
                        txtReceivedSummary.setText(receivedText.toString().trim());
                        txtOutstandingSummary.setText(outstandingText.toString().trim());
                    });
                }
                catch (JSONException e){
                    runOnUiThread(() -> {
                        txtDonatedSummary.setText("Error parsing data.");
                        txtReceivedSummary.setText("Error parsing data.");
                        txtOutstandingSummary.setText("Error parsing data.");
                    });
                }
            }
        });
    }

    private void fetchDonorContacts(int userId, TextView donorContactsView) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/get_donors.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> donorContactsView.setText("Failed to load donors."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> donorContactsView.setText("Error loading donors."));
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject obj = new JSONObject(jsonData);

                    if (obj.getBoolean("success")) {
                        JSONArray donors = obj.getJSONArray("donors");
                        SpannableStringBuilder builder = new SpannableStringBuilder();

                        if (donors.length() == 0) {
                            builder.append("No one has donated to you yet.");
                        } else {

                            for (int i = 0; i < donors.length(); i++) {
                                JSONObject donor = donors.getJSONObject(i);
                                builder.append("•  ");

                                String name = donor.getString("name");
                                SpannableString underlinedName = new SpannableString(name);
                                underlinedName.setSpan(new UnderlineSpan(), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                builder.append(underlinedName);

                                builder.append(": \n   ")
                                        .append(donor.getString("email"))
                                        .append("\n");
                            }

                        }

                        runOnUiThread(() -> donorContactsView.setText(builder));
                    } else {
                        runOnUiThread(() -> donorContactsView.setText("No donor data available."));
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> donorContactsView.setText("Error parsing donor data."));
                }
            }
        });
    }


}