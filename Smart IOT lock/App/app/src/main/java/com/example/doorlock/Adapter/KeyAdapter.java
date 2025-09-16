package com.example.doorlock.Adapter;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KeyAdapter extends RecyclerView.Adapter<KeyAdapter.KeyViewHolder> {

    private List<Key> keyList;
    private String keyUid;
    private String userNic; // Store NIC of the user

    public KeyAdapter(List<Key> keyList, String userNic) {
        this.keyList = keyList;
        this.userNic = userNic; // Assign NIC
    }

    /*
    public KeyAdapter(List<Key> keyList) {
        this.keyList = keyList;
    }

     */

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_key, parent, false);
        return new KeyViewHolder(view);
    }


    /*
    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        Key key = keyList.get(position);
        holder.keyName.setText(key.getKeyName());

        holder.unlockButton.setOnClickListener(v -> {
            toggleDoorState(key.getKeyName(), holder);
        });
    }

     */

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        Key key = keyList.get(position);
        holder.keyName.setText(key.getKeyName());

        holder.unlockButton.setOnClickListener(v -> {
            toggleDoorState(key, holder); // Pass the Key object instead of just the name
        });
    }


    @Override
    public int getItemCount() {
        return keyList.size();
    }

    public static class KeyViewHolder extends RecyclerView.ViewHolder {
        TextView keyName;
        Button unlockButton;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            keyName = itemView.findViewById(R.id.key_name);
            unlockButton = itemView.findViewById(R.id.unlock_button);
        }
    }



    //5
    /*
    private void toggleDoorState(Key key, KeyViewHolder holder) {
        DatabaseReference doorsRef = FirebaseDatabase.getInstance().getReference("Doors");
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("log");

        doorsRef.orderByChild("UUID").equalTo(key.getKeyUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                        String doorKey = doorSnapshot.getKey(); // Get the door UID key

                        // Unlock door
                        DatabaseReference stateRef = doorsRef.child(doorKey).child("state");
                        stateRef.setValue(true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Door unlocked!", Toast.LENGTH_SHORT).show();

                                // Fetch current log count to assign a proper log number
                                logRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot logSnapshot) {
                                        long count = logSnapshot.getChildrenCount() + 1; // Get the next log number
                                        String logNumber = String.format("%02d", count); // Format as 01, 02, etc.

                                        // Create log message with count
                                        String logMessage = logNumber + " " + key.getKeyName() + " has unlocked by " + userNic;
                                        String logId = logRef.push().getKey(); // Generate unique log ID
                                        logRef.child(logId).setValue(logMessage);

                                        // Wait 10 seconds, then lock the door
                                        new android.os.Handler().postDelayed(() -> {
                                            stateRef.setValue(false).addOnCompleteListener(revertTask -> {
                                                if (revertTask.isSuccessful()) {
                                                    Toast.makeText(holder.itemView.getContext(), "Door locked again!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(holder.itemView.getContext(), "Failed to lock the door!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }, 10000);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(holder.itemView.getContext(), "Failed to retrieve log count!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Unlock failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Door not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.itemView.getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

     */

    private void toggleDoorState(Key key, KeyViewHolder holder) {
        DatabaseReference doorsRef = FirebaseDatabase.getInstance().getReference("Doors");
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("log");

        doorsRef.orderByChild("UUID").equalTo(key.getKeyUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                        String doorKey = doorSnapshot.getKey(); // Get the door UID key

                        // Unlock door
                        DatabaseReference stateRef = doorsRef.child(doorKey).child("state");
                        stateRef.setValue(true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Door unlocked!", Toast.LENGTH_SHORT).show();

                                // Fetch current log count to assign a proper log number
                                logRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot logSnapshot) {
                                        long count = logSnapshot.getChildrenCount() + 1; // Get the next log number
                                        String logNumber = String.format("%02d", count); // Format as 01, 02, etc.

                                        // Get current date and time
                                        String currentTime = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date()); // 24-hour format (HHmm)
                                        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()); // Date in dd.MM.yyyy format

                                        // Create log message with count, timestamp, and user info
                                        String logMessage = logNumber + " " + key.getKeyName() + " has unlocked by " + userNic + ", " + currentTime + "h on " + currentDate;
                                        String logId = logRef.push().getKey(); // Generate unique log ID
                                        logRef.child(logId).setValue(logMessage);

                                        // Wait 10 seconds, then lock the door
                                        new android.os.Handler().postDelayed(() -> {
                                            stateRef.setValue(false).addOnCompleteListener(revertTask -> {
                                                if (revertTask.isSuccessful()) {
                                                    Toast.makeText(holder.itemView.getContext(), "Door locked again!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(holder.itemView.getContext(), "Failed to lock the door!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }, 10000);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(holder.itemView.getContext(), "Failed to retrieve log count!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Unlock failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Door not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.itemView.getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





}
