package com.example.lendahand;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import okhttp3.Response;
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
    }

    private void fetchDonorsFromDatabase() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://edition.cnn.com/")        //NADAV INSERT HERE
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> populateLeaderboardFromJSON(responseData));
                }
            }

        });
    }

    private void populateLeaderboardFromJSON(String jsonData) {
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject donor = jsonArray.getJSONObject(i);
                String name = donor.getString("name");
                double amount = donor.getInt("amount");
                addDonorRow(i + 1, name, amount);
                }
            } catch (JSONException e) {
                e.printStackTrace();
        }
    }
    private void addDonorRow(int position,String name,double amount){
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 16, 16, 16);

        TextView positionview = new TextView(this);
        positionview.setText(position + ". ");

        TextView nameView = new TextView(this);
        nameView.setText(name);

        TextView amountView = new TextView(this);
        amountView.setText("R" + amount);

        row.addView(positionview);
        row.addView(nameView);
        row.addView(amountView);

        leaderboard.addView(row);
    }
}