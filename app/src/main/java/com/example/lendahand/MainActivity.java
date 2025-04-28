package com.example.lendahand;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView textLogin = findViewById(R.id.here);
        textLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Donorwall.class);
            startActivity(intent);
        });
    }

}