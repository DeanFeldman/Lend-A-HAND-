package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import okhttp3.Response;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Donorwall extends AppCompatActivity {

    private LinearLayout leaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donorwall);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.donorwall_scrollview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        leaderboard = findViewById(R.id.leaderboardContainer);
        fetchDonorsFromDatabase();

        Button buttonProfile = findViewById(R.id.button_Profile);
        buttonProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Donorwall.this, Profile.class);
            startActivity(intent);
        });

        Button buttonDonate = findViewById(R.id.button_Donate);
        buttonDonate.setOnClickListener(view -> {
            Intent intent = new Intent(Donorwall.this, Donate.class);
            startActivity(intent);
        });

        Button buttonRecieve = findViewById(R.id.button_Recieve);
        buttonRecieve.setOnClickListener(view -> {
            Intent intent = new Intent(Donorwall.this, Recieve.class);
            startActivity(intent);
        });
    }

    private void fetchDonorsFromDatabase() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/donor_wall.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> populateLeaderboardFromJSON(responseData));
                }
            }

        });
    }

    private void populateLeaderboardFromJSON(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("leaderboard"); // <--- fix here
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject donor = jsonArray.getJSONObject(i);

                String firstName = donor.optString("first_name", "");
                String lastName = donor.optString("last_name", "");
                double amount = donor.optDouble("total_donated", 0);

                String name = (firstName + " " + lastName).trim(); // combine names
                if (name.isEmpty()) {
                    name = "Anonymous";
                }

                addDonorRow(i + 1, name, amount);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addDonorRow(int position, String name, double amount) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 16, 16, 16);

        // Create layout params to share space
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // weight
        );

        TextView positionView = new TextView(this);
        positionView.setText(position + ". ");
        positionView.setTextSize(16); // Bigger text
        positionView.setLayoutParams(params);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(18); // Bigger text
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        nameView.setLayoutParams(params);

        TextView amountView = new TextView(this);
        amountView.setText("R" + String.format("%.2f", amount));
        amountView.setTextSize(16);
        amountView.setLayoutParams(params);
        amountView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);

        // Add views to the row
        row.addView(positionView);
        row.addView(nameView);
        row.addView(amountView);

        // Optionally add a small bottom divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        ));
        divider.setBackgroundColor(0xFFCCCCCC);

        leaderboard.addView(row);
        leaderboard.addView(divider);
    }

}