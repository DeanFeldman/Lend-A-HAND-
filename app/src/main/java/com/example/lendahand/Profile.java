package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends AppCompatActivity {

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

        Button buttonLogin = findViewById(R.id.button_save_profile);
        buttonLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Profile.class);
            startActivity(intent);
        });

        Button buttonDonorWall = findViewById(R.id.button_DonorWall);
        buttonDonorWall.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Donorwall.class);
            startActivity(intent);
        });

        Button buttonDonate = findViewById(R.id.button_Donate);
        buttonDonate.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Donate.class);
            startActivity(intent);
        });

        Button buttonRecieve = findViewById(R.id.button_Recieve);
        buttonRecieve.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, Recieve.class);
            startActivity(intent);
        });


    }
}