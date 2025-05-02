package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Donate extends AppCompatActivity {

    private Spinner spinnerItems;
    private int user_id;
    EditText qty;
    private ArrayAdapter<String> adapter;
    private RecyclerView recyclerView;
    private ReceiverAdapter receiverAdapter;
    private List<Receiver> receiverList = new ArrayList<>();
    private Button donateButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.donate_scrollview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.receiver_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiverAdapter = new ReceiverAdapter(this, receiverList, this::checkDonationSum);
        recyclerView.setAdapter(receiverAdapter);

        qty = findViewById(R.id.input_quantity);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        user_id = prefs.getInt("user_id", -1);
        if (user_id == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        donateButton = findViewById(R.id.button_donate_to_receiver);
        donateButton.setOnClickListener(v -> {
            for (Receiver r : receiverList) {
                if (r.quantityToDonate > 0) {
                    sendDonation(r);
                }
            }
        });

        Button buttonDonorWall = findViewById(R.id.button_DonorWall);
        buttonDonorWall.setOnClickListener(view -> {
            Intent intent = new Intent(Donate.this, Donorwall.class);
            startActivity(intent);
        });

        Button buttonProfile = findViewById(R.id.Button_Profile);
        buttonProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Donate.this, Profile.class);
            startActivity(intent);
        });

        Button buttonRecieve = findViewById(R.id.button_Recieve);
        buttonRecieve.setOnClickListener(view -> {
            Intent intent = new Intent(Donate.this, Recieve.class);
            startActivity(intent);
        });

        qty = findViewById(R.id.input_quantity);
        //fill the arrays
        spinnerItems = findViewById(R.id.spinner_items);
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<String>());

        spinnerItems.setAdapter(adapter);
        spinnerItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchReceiversFromDatabase();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        new Thread(() -> {
            ItemList list = new ItemList();
            list.FetchItems(Donate.this);

            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(ItemList.getItems());
                adapter.notifyDataSetChanged();
            });
        }).start();

    }

    private void fetchReceiversFromDatabase() {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("item_name", spinnerItems.getSelectedItem().toString())
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698 600/get_needed_items.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    try {
                        JSONArray jsonArray = new JSONArray(json);
                        receiverList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            int requestId = obj.getInt("request_id");
                            int userId = obj.getInt("user_id");
                            String name = obj.getString("user_fname") + " " + obj.getString("user_lname");
                            String bio = obj.getString("user_biography");
                            int needed = obj.getInt("quantity_needed");

                            receiverList.add(new Receiver(requestId, userId, name, bio, needed));
                        }

                        runOnUiThread(() -> receiverAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkDonationSum() {
        int totalAllocated = 0;
        int totalAvailable = 0;


        String input = qty.getText().toString().trim();
        if (!input.isEmpty()) {
            try {
                totalAvailable = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                totalAvailable = 0;
            }
        }

        // Sum allocations and validate per receiver
        for (Receiver r : receiverList) {
            if (r.quantityToDonate > r.quantityNeeded) {
                donateButton.setVisibility(View.GONE);
                Toast.makeText(this, "Cannot allocate more than " + r.quantityNeeded + " to " + r.name, Toast.LENGTH_SHORT).show();
                return;
            }
            totalAllocated += r.quantityToDonate;
        }

        // Final logic: allow if totalAllocated > 0 and ≤ totalAvailable
        if (totalAllocated > 0 && totalAllocated <= totalAvailable) {
            donateButton.setVisibility(View.VISIBLE);
        } else {
            donateButton.setVisibility(View.GONE);
        }
    }


    private void sendDonation(Receiver r) {
        OkHttpClient client = new OkHttpClient();

        int donor_user_id = user_id;
        int request_id = r.getRequestId();
        int quantity = r.quantityToDonate;
        int newRemaining = r.quantityNeeded - quantity;


        RequestBody donationForm = new FormBody.Builder()
                .add("donor_user_id", String.valueOf(donor_user_id))
                .add("request_id", String.valueOf(request_id))
                .add("quantity_donated", String.valueOf(quantity))
                .build();

        Request insertDonationRequest = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/send_donated_items.php")
                .post(donationForm)
                .build();

        client.newCall(insertDonationRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(Donate.this, "Donation insert failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(Donate.this, "Insert donation error", Toast.LENGTH_SHORT).show());
                    return;
                }


                RequestBody updateForm = new FormBody.Builder()
                        .add("request_id", String.valueOf(request_id))
                        .add("new_quantity", String.valueOf(newRemaining))
                        .build();

                Request updateRequest = new Request.Builder()
                        .url("https://lamp.ms.wits.ac.za/home/s2698600/update_request_items.php")
                        .post(updateForm)
                        .build();

                client.newCall(updateRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(Donate.this, "Request update failed", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(Donate.this, "Failed to update request", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        runOnUiThread(() -> {
                            r.quantityNeeded = newRemaining;
                            r.quantityToDonate = 0;

                            int currentQty = 0;
                            try {
                                currentQty = Integer.parseInt(qty.getText().toString().trim());
                            } catch (NumberFormatException e) {
                                currentQty = 0;
                            }

                            int updatedQty = Math.max(0, currentQty - quantity);
                            qty.setText(String.valueOf(updatedQty));

                            Toast.makeText(Donate.this, "Donation sent!", Toast.LENGTH_SHORT).show();
                            receiverAdapter.notifyDataSetChanged();
                            checkDonationSum();
                        });
                    }
                });
            }
        });
    }

}