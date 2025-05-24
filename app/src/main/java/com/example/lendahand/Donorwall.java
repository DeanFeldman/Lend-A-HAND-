package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

        Button btnProfile = findViewById(R.id.button_Profile);
        btnProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Donorwall.this, Profile.class);
            startActivity(intent);
        });

        Button btnDonate = findViewById(R.id.button_Donate);
        btnDonate.setOnClickListener(view -> {
            Intent intent = new Intent(Donorwall.this, Donate.class);
            startActivity(intent);
        });

        Button btnRecieve = findViewById(R.id.button_Recieve);
        btnRecieve.setOnClickListener(view -> {
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
            JSONArray jsonArray = jsonObject.getJSONArray("leaderboard");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject donor = jsonArray.getJSONObject(i);

                String firstName = donor.optString("first_name", "");
                String lastName = donor.optString("last_name", "");
                double amount = donor.optDouble("total_donated", 0);

                String name = (firstName + " " + lastName).trim();
                addDonorRow(i + 1, name, amount);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addDonorRow(int position, String name, double amount) {
        View card = getLayoutInflater().inflate(R.layout.item_donor_card, leaderboard, false);

        TextView rankView = card.findViewById(R.id.text_rank);
        TextView nameView = card.findViewById(R.id.text_name);
        TextView amountView = card.findViewById(R.id.text_amount);

        
        nameView.setText(name);
        amountView.setText((int) amount + " items");

        switch (position) {
            case 1:
                rankView.setText("🥇 " + position + ".");
                nameView.setTextColor(0xFFFFD700);
                break;
            case 2:
                rankView.setText("🥈 " + position + ".");
                nameView.setTextColor(0xFFC0C0C0);
                break;
            case 3:
                rankView.setText("🥉 " + position + ".");
                nameView.setTextColor(0xFFCD7F32);
                break;
            default:
                rankView.setText(position + ".");
                break;
        }

        leaderboard.addView(card);
    }


}