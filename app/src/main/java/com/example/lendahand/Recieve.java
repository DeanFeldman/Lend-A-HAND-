package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Recieve extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recieve);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.receiver_scrollview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonDonorWall = findViewById(R.id.button_donorwall);
        buttonDonorWall.setOnClickListener(view -> {
            Intent intent = new Intent(Recieve.this, Donorwall.class);
            startActivity(intent);
        });

        Button buttonProfile = findViewById(R.id.button_profile);
        buttonProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Recieve.this, Profile.class);
            startActivity(intent);
        });

        Button buttonDonate = findViewById(R.id.button_donate);
        buttonDonate.setOnClickListener(view -> {
            Intent intent = new Intent(Recieve.this, Donate.class);
            startActivity(intent);
        });
    }
}