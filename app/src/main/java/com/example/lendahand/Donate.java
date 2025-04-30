package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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