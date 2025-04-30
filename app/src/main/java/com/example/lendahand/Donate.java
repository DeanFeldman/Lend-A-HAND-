package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Donate extends AppCompatActivity {

    private Spinner spinnerItems;
    private ArrayAdapter<String> adapter;

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
}