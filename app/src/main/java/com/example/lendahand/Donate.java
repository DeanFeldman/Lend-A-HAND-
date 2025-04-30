package com.example.lendahand;

import android.content.Intent;
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
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Donate extends AppCompatActivity {

    private Spinner spinnerItems;
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
        donateButton = findViewById(R.id.button_donate_to_receiver);
        donateButton.setVisibility(View.GONE);


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
                .url("https://lamp.ms.wits.ac.za/home/s2698600/getneededreciever.php")
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

                            String name = obj.getString("user_fname") + " " + obj.getString("user_lname");
                            String bio = obj.getString("user_biography");
                            int needed = obj.getInt("quantity_needed");

                            receiverList.add(new Receiver(0, name, bio, needed));
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
        int total = 0;
        for (Receiver r : receiverList) {
            total += r.quantityToDonate;
        }

        int expected = 0;
        try {
            expected = Integer.parseInt(qty.getText().toString().trim());
        } catch (NumberFormatException e) {
            expected = 0;
        }

        donateButton.setVisibility((total == expected && total > 0) ? View.VISIBLE : View.GONE);
    }

}