package com.example.lendahand;

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
        TextView txtName, txtNeeded, txtUserBio;
        EditText txtDonationInput;
        TextWatcher currentWatcher;
        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.text_receiver_name);
            txtNeeded = view.findViewById(R.id.text_quantity_needed);
            txtDonationInput = view.findViewById(R.id.input_donation);
            txtUserBio = view.findViewById(R.id.text_receiver_bio);
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

        holder.txtUserBio.setText(receiver.biography);
        holder.txtName.setText(receiver.name);
        holder.txtNeeded.setText("Needs: " + receiver.quantityNeeded);
        holder.txtDonationInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        holder.txtDonationInput.setText(String.valueOf(receiver.quantityToDonate));

        // Remove previous watcher if exists
        if (holder.currentWatcher != null) {
            holder.txtDonationInput.removeTextChangedListener(holder.currentWatcher);
        }

        // Create and assign a new watcher
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                int value = 0;
                try {
                    value = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    // Leave value as 0
                }

                if (value < 0) value = 0;

                if (value > receiver.quantityNeeded) {
                    CUSTOMTOAST.showCustomToast(context, "Cannot donate more than " + receiver.quantityNeeded + " to " + receiver.name);
                    value = receiver.quantityNeeded;
                }

                receiver.quantityToDonate = value;

                if (!input.equals(String.valueOf(value))) {
                    holder.txtDonationInput.setText(String.valueOf(value));
                    holder.txtDonationInput.setSelection(holder.txtDonationInput.getText().length());
                }

                donationChangedListener.onDonationChanged();
            }
        };

        holder.txtDonationInput.addTextChangedListener(watcher);
        holder.currentWatcher = watcher;
    }


    @Override
    public int getItemCount() {
        return receiverList.size();
    }
}
