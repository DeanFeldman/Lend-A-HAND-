package com.example.lendahand;

import android.widget.Toast;

import org.json.JSONArray;
import android.app.Activity;
import android.content.Context;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.List;

public class ItemList {

    public static List<String> ITEMS = new ArrayList<>();

    public void FetchItems(Context context) {
        OkHttpClient client  = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://lamp.ms.wits.ac.za/home/s2698600/items.php")
                .build();

        try{
            Response response = client.newCall(request).execute();
            if(response.isSuccessful() && response.body() != null){
                String responseBody = response.body().string();

                JSONArray jsonArray = new JSONArray(responseBody);

                ITEMS.clear();
                ITEMS.add("Please select an item below          ▼");

                for (int i = 0; i < jsonArray.length(); i++) {
                    String item = jsonArray.getString(i);
                    ITEMS.add(item);
                }


            } else {
                ((Activity) context).runOnUiThread(() ->
                        CUSTOMTOAST.showCustomToast(context, "Failed to fetch items")
                );
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            ((Activity) context).runOnUiThread(() ->
                    CUSTOMTOAST.showCustomToast(context, "Error fetching items")
            );
        }
    }

    public static List<String> getItems() {
        return ITEMS;
    }
}
