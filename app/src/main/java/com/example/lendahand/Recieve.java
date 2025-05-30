package com.example.lendahand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Recieve extends AppCompatActivity {
    private Spinner spinnerItems;
    private ArrayAdapter<String> adapter;
    private EditText txtQuantityInput;
    private int user_id;
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


        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        user_id = prefs.getInt("user_id", -1);
        if (user_id == -1) {
            CUSTOMTOAST.showCustomToast(this, "User not logged in");
            finish();
            return;
        }
        spinnerItems = findViewById(R.id.spinner_needed_items);
        txtQuantityInput = findViewById(R.id.input_needed_quantity);
        Button btnSubmit = findViewById(R.id.button_add_request);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerItems.setAdapter(adapter);

        // Load item list (background thread)
        new Thread(() -> {
            ItemList list = new ItemList();
            list.FetchItems(Recieve.this);

            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(ItemList.getItems());
                adapter.notifyDataSetChanged();
            });
        }).start();

        btnSubmit.setOnClickListener(v -> {
            submitRequest();
        });

        Button buttonback = findViewById(R.id.button_back);
        buttonback.setOnClickListener(view -> {
            Intent intent = new Intent(Recieve.this, Donorwall.class);
            startActivity(intent);
        });

    }
    private void submitRequest() {
        String itemName = spinnerItems.getSelectedItem().toString();
        String quantityStr = txtQuantityInput.getText().toString().trim();

        if (quantityStr.isEmpty()) {
            CUSTOMTOAST.showCustomToast(this, "Please enter quantity");
            return;
        }

        int quantity = Integer.parseInt(quantityStr);

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(user_id))
                .add("item_name", itemName)
                .add("quantity_needed", String.valueOf(quantity))
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/submit_request.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        CUSTOMTOAST.showCustomToast(Recieve.this, "Network error: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    CUSTOMTOAST.showCustomToast(Recieve.this, "Request submitted successfully!");
                    txtQuantityInput.setText("");
                });
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String userEmail = prefs.getString("user_email", "you@example.com");
                    String userName = prefs.getString("user_fname", "User");
                    String itemName = spinnerItems.getSelectedItem().toString();

                    //send email
                    String subject = "Donation request received!";
                    String body = "Hi " + userName + ",\n\nYou successfully submitted a request for " +
                            quantityStr + " " + itemName + ".\nWe'll notify you when a donation is made.\n\nThank you!";

                    new Thread(() -> {
                        EmailSender sender = new EmailSender();
                        sender.sendEmail(userEmail, subject, body);
                    }).start();

            }
        });
    }
}