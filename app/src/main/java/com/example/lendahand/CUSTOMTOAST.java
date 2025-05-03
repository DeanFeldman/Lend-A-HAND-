package com.example.lendahand;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lendahand.R;

public class CUSTOMTOAST {
    public static void showCustomToast(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_message);
        text.setText(message);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.lah_logo);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
