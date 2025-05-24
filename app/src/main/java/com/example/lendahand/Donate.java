package com.example.lendahand;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Donate extends AppCompatActivity {

    private Spinner spnItems;
    private int user_id;
    EditText txtQty;
    private ArrayAdapter<String> adapter;
    private RecyclerView recyclerView;
    private ReceiverAdapter receiverAdapter;
    private final List<Receiver> receiverList = new ArrayList<>();
    private Button btnDonate;
    private int qty;

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

        txtQty = findViewById(R.id.input_quantity);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        user_id = prefs.getInt("user_id", -1);
        if (user_id == -1) {
            CUSTOMTOAST.showCustomToast(this, "User not logged in");
            finish();
            return;
        }

        btnDonate = findViewById(R.id.button_donate_to_receiver);
        btnDonate.setOnClickListener(v -> {
            for (Receiver r : receiverList) {
                if (r.quantityToDonate > 0) {
                    sendDonation(r);
                }
            }
        });

        Button buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(view -> {
            Intent intent = new Intent(Donate.this, Donorwall.class);
            startActivity(intent);
        });


        spnItems = findViewById(R.id.spinner_items);
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<String>());

        spnItems.setAdapter(adapter);

        spnItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        Object selectedItem = spnItems.getSelectedItem();
        if (selectedItem == null) {
            runOnUiThread(() ->
                    CUSTOMTOAST.showCustomToast(Donate.this, "Please select an item first.")
            );
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("item_name", spnItems.getSelectedItem().toString())
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/get_needed_items.php")
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

                    Log.d("DONATE_DEBUG", "Response: " + json);

                    try {
                        JSONArray jsonArray = new JSONArray(json);
                        receiverList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            int requestId = obj.getInt("request_id");
                            int userId = obj.getInt("user_id");
                            int needed = obj.getInt("quantity_needed");

                            String name = obj.getString("user_fname") + " " + obj.getString("user_lname");
                            String bio = obj.getString("user_biography");
                            String email = obj.getString("user_email");

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            int user_id = prefs.getInt("user_id", -1);

                            if(user_id!=userId) {
                                receiverList.add(new Receiver(requestId, userId, name, email, bio, needed));
                            }
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


        String input = txtQty.getText().toString().trim();
        if (!input.isEmpty()) {
            try {
                totalAvailable = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                totalAvailable = 0;
            }
        }

        for (Receiver r : receiverList) {
            if (r.quantityToDonate > r.quantityNeeded) {
                btnDonate.setVisibility(View.GONE);
                CUSTOMTOAST.showCustomToast(this, "Cannot allocate more than " + r.quantityNeeded + " to " + r.name);
                return;
            }
            totalAllocated += r.quantityToDonate;
        }

        if (totalAllocated > 0) {
            btnDonate.setVisibility(View.VISIBLE);
        } else {
            btnDonate.setVisibility(View.GONE);
        }

        if (totalAllocated > totalAvailable) {
            btnDonate.setVisibility(View.GONE);
            CUSTOMTOAST.showCustomToast(this, "You've allocated more than your available amount.");

        }

    }


    private void sendDonation(Receiver r){
        OkHttpClient client = new OkHttpClient();

        int donor_user_id = user_id;
        int request_id = r.getRequestId();
        int quantity = r.quantityToDonate;
        qty=quantity;
        if(qty%2==0) qty--;
        if(qty>7) qty=7;
        int newRemaining = r.quantityNeeded - quantity;


        RequestBody formBody = new FormBody.Builder()
                .add("donor_user_id", String.valueOf(donor_user_id))
                .add("request_id", String.valueOf(request_id))
                .add("quantity_donated", String.valueOf(quantity))
                .build();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/send_donated_items.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback(){

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->  CUSTOMTOAST.showCustomToast(Donate.this, "Failed to send donation"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    RequestBody formBody1 = new FormBody.Builder()
                            .add("donor_user_id", String.valueOf(donor_user_id))
                            .add("request_id", String.valueOf(request_id))
                            .add("quantity_donated", String.valueOf(quantity))
                            .add("new_quantity", String.valueOf(newRemaining))
                            .build();

                    Request request2 = new Request.Builder()
                            .url("https://lamp.ms.wits.ac.za/home/s2698600/update_request_items.php")
                            .post(formBody1)
                            .build();

                    runOnUiThread(() -> {
                        String currentQtyStr = txtQty.getText().toString().trim();
                        int currentQty = 0;
                        try {
                            currentQty = Integer.parseInt(currentQtyStr);
                        } catch (NumberFormatException e) {
                            currentQty = 0;
                        }

                        int remaining = Math.max(0, currentQty - quantity);
                        txtQty.setText(String.valueOf(remaining));

                        r.quantityNeeded = newRemaining;
                        r.quantityToDonate = 0;

                        CUSTOMTOAST.showCustomToast(Donate.this, "Donation sent!");
                        receiverAdapter.notifyDataSetChanged();
                        checkDonationSum();
                    });
                    

                    client.newCall(request2).enqueue(new okhttp3.Callback(){

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if(response.isSuccessful()){
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                String donorEmail = prefs.getString("user_email", "donor@example.com");
                                String donorName = prefs.getString("user_fname", "Donor");
                                String receiverEmail = r.getEmail();
                                String receiverName = r.getName();

                                runOnUiThread(() -> {



                                    showSuccessDialog(() -> {
                                        // This runs after the success dialog is dismissed
                                        new AlertDialog.Builder(Donate.this)
                                                .setTitle("Emails Sent")
                                                .setMessage("Confirmation email sent to:\n" + donorEmail +
                                                        "\nNotification email sent to:\n" + receiverEmail)
                                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                .show();
                                    });
                                });
                                runOnUiThread(() ->{


                                    //send emails
                                    new Thread(() -> {


                                        String itemName = spnItems.getSelectedItem().toString();

                                        EmailSender sender = new EmailSender();

                                        // Email to donor
                                        String donorSubject = "Donation Confirmation";
                                        String donorBody = "Hi " + donorName + ",\n\nYou successfully donated " + quantity + " " + itemName +
                                                " to " + receiverName + " (" + receiverEmail + ").\nThank you for your generosity!";
                                        sender.sendEmail(donorEmail, donorSubject, donorBody);

                                        // Email to receiver
                                        String receiverSubject = "You've Received a Donation!";
                                        String receiverBody = "Hi " + receiverName + ",\n\nYou have received " + quantity + " " + itemName +
                                                " from " + donorName + " (" + donorEmail + ").\nPlease check your account for updates.";
                                        sender.sendEmail(receiverEmail, receiverSubject, receiverBody);


                                    }).start();

                                });
                            }
                            else{
                                runOnUiThread(() ->  CUSTOMTOAST.showCustomToast(Donate.this, "Request update failed"));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() ->  CUSTOMTOAST.showCustomToast(Donate.this, "Failed to update request"));
                        }
                    });
                } else {
                    runOnUiThread(() ->  CUSTOMTOAST.showCustomToast(Donate.this, "Failed to record donation"));
                }
            }


        });
    }
    private void showSuccessDialog(Runnable onDismiss) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Donate.this, R.style.TransparentDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_donation_success, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        ImageView heart = dialogView.findViewById(R.id.img_heart);
        ImageView logo = dialogView.findViewById(R.id.img_logo);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        heart.setVisibility(View.INVISIBLE);
        heart.setAlpha(1f);
        heart.setScaleX(0f);
        heart.setScaleY(0f);

        new Handler().postDelayed(() -> {
            heart.setVisibility(View.VISIBLE);

            ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
            pulseAnimator.setDuration(500);
            pulseAnimator.setRepeatCount(2+qty);
            pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            pulseAnimator.setInterpolator(new OvershootInterpolator());

            pulseAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                heart.setScaleX(1f + 0.08f * value);
                heart.setScaleY(1f + 0.08f * value);
            });

            pulseAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    showFlyingHeartsAnimation(logo);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Fade out heart and logo at the end
                    AnimatorSet exitSet = new AnimatorSet();
                    exitSet.playTogether(
                            ObjectAnimator.ofFloat(heart, View.ALPHA, 1f, 0f),
                            ObjectAnimator.ofFloat(logo, View.ALPHA, 1f, 0f)
                    );
                    exitSet.setDuration(500);
                    exitSet.setStartDelay(300+qty* 300L);
                    exitSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            dialog.dismiss();
                            if (onDismiss != null) onDismiss.run();
                        }
                    });
                    exitSet.start();
                }
            });

            pulseAnimator.start();

        }, 1000);
    }

    private void showFlyingHeartsAnimation(ImageView sourceHeart) {
        FrameLayout heartContainer = findViewById(R.id.heart_animation_overlay);
        if (heartContainer == null || sourceHeart == null) return;

        heartContainer.post(() -> {

            int[] sourceLoc = new int[2];
            int[] containerLoc = new int[2];

            sourceHeart.getLocationOnScreen(sourceLoc);
            heartContainer.getLocationOnScreen(containerLoc);

            float startX =sourceLoc[0] - containerLoc[0]  + (sourceHeart.getWidth() / 3);
            float startY = sourceLoc[1] - containerLoc[1] + (sourceHeart.getHeight() / 3);


            for (int i = 0; i <qty*3; i++) {
                final ImageView heart = new ImageView(this);
                heart.setImageResource(R.drawable.heart);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(300, 300);
                heart.setLayoutParams(lp);
                heart.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                heart.setAlpha(1f);

                heartContainer.addView(heart);

                heart.setTranslationX(startX);
                heart.setTranslationY(startY);

                float randomX = (float) (Math.random() * 600 - 300);
                float randomY = (float) -(Math.random() * 800 + 200);

                ObjectAnimator translateX = ObjectAnimator.ofFloat(heart, "translationX", startX, startX + randomX);
                ObjectAnimator translateY = ObjectAnimator.ofFloat(heart, "translationY", startY, startY + randomY);
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(heart, "alpha", 1f, 0f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(heart, "scaleX", 1f, 1.5f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(heart, "scaleY", 1f, 1.5f);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(translateX, translateY, fadeOut, scaleX, scaleY);
                set.setDuration(1500 + (int) (Math.random() * 500));
                set.setStartDelay(i * 100L);
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        heartContainer.removeView(heart);
                    }
                });
                set.start();
            }
        });
    }





}