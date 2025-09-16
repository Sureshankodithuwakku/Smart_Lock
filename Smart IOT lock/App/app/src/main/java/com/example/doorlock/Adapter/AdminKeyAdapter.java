package com.example.doorlock.Adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doorlock.Model.Key;
import com.example.doorlock.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminKeyAdapter extends RecyclerView.Adapter<AdminKeyAdapter.KeyViewHolder> {

    private List<Key> keyList;
    private String nic;

    public AdminKeyAdapter(List<Key> keyList, String nic, DatabaseReference databaseReference) {
        this.keyList = keyList;
        this.nic = nic;
        this.databaseReference = databaseReference;
    }

    private DatabaseReference databaseReference;

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_key, parent, false);
        return new KeyViewHolder(view);
    }

    //2
    /*
    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        Key key = keyList.get(position);
        String keyUid = key.getKeyUid(); // Get UUID
        String keyName = key.getKeyName(); // Get Name

        holder.keyName.setText(keyName); // Show key name instead of UUID

        holder.removeKeyButton.setOnClickListener(v -> {
            if (nic == null || nic.isEmpty()) {
                Toast.makeText(v.getContext(), "User NIC is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            if (keyUid == null || keyUid.isEmpty()) {
                Toast.makeText(v.getContext(), "Key ID is invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to remove this key?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove key from Firebase under Users/{nic}/keys/{keyUid}
                        DatabaseReference userKeysRef = databaseReference.getRoot().child("Users").child(nic).child("keys");
                        userKeysRef.child(keyUid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove key from list and notify adapter
                                    keyList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, keyList.size());
                                    Toast.makeText(v.getContext(), "Key removed successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to remove key", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

     */

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        Key key = keyList.get(position);
        String keyUid = key.getKeyUid(); // UUID
        String keyName = key.getKeyName(); // door_name

        holder.keyName.setText(keyName); // Display door_name

        holder.removeKeyButton.setOnClickListener(v -> {
            if (nic == null || nic.isEmpty()) {
                Toast.makeText(v.getContext(), "User NIC is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            if (keyUid == null || keyUid.isEmpty()) {
                Toast.makeText(v.getContext(), "Key ID is invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to remove this key?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove key from Firebase under Users/{nic}/keys/{keyUid}
                        DatabaseReference userKeysRef = databaseReference.getRoot().child("Users").child(nic).child("keys");
                        userKeysRef.child(keyUid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove key from list and notify adapter
                                    keyList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, keyList.size());
                                    Toast.makeText(v.getContext(), "Key removed successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to remove key", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }





    @Override
    public int getItemCount() {
        return keyList.size();
    }

    public static class KeyViewHolder extends RecyclerView.ViewHolder {
        TextView keyName;
        Button removeKeyButton;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            keyName = itemView.findViewById(R.id.key_name);
            removeKeyButton = itemView.findViewById(R.id.remove_key_button);
        }
    }
}
