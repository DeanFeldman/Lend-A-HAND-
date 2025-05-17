package com.example.lendahand;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReceiverAdapter extends RecyclerView.Adapter<ReceiverAdapter.ViewHolder> {

    private final List<Receiver> receiverList;
    private final Context context;
    private final OnDonationChangedListener donationChangedListener;

    public interface OnDonationChangedListener {
        void onDonationChanged();
    }

    public ReceiverAdapter(Context context, List<Receiver> receivers, OnDonationChangedListener listener) {
        this.context = context;
        this.receiverList = receivers;
        this.donationChangedListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, neededText, userBio;
        EditText donationInput;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.text_receiver_name);
            neededText = view.findViewById(R.id.text_quantity_needed);
            donationInput = view.findViewById(R.id.input_donation);
            userBio = view.findViewById(R.id.text_receiver_bio);
        }
    }

    @NonNull
    @Override
    public ReceiverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receiver_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiverAdapter.ViewHolder holder, int position) {
        Receiver receiver = receiverList.get(position);

        holder.userBio.setText(receiver.biography);
        holder.nameText.setText(receiver.name);
        holder.neededText.setText("Needs: " + receiver.quantityNeeded);

        holder.donationInput.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(3)
        });

        holder.donationInput.setText(String.valueOf(receiver.quantityToDonate));

        holder.donationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                int value = 0;
                try {
                    value = Integer.parseInt(input);
                }
                catch (NumberFormatException e) { }

                if (value < 0) value = 0;

                if (value > receiver.quantityNeeded) {
                    CUSTOMTOAST.showCustomToast(context, "Cannot donate more than " + receiver.quantityNeeded + " to " + receiver.name);
                    value = receiver.quantityNeeded;
                }

                if (value > receiver.quantityNeeded){
                    value = receiver.quantityNeeded;
                }

                receiver.quantityToDonate = value;

                if (!input.equals(String.valueOf(value))) {
                    holder.donationInput.setText(String.valueOf(value));
                    holder.donationInput.setSelection(holder.donationInput.getText().length());
                }

                donationChangedListener.onDonationChanged();
            }

        });

    }

    @Override
    public int getItemCount() {
        return receiverList.size();
    }
}
