package com.example.doorlock;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandOverKey extends AppCompatActivity {

    private AutoCompleteTextView doorListSpinner, userListSpinner;
    private Button sendKeyButton, backButton;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private List<String> doorList, userList;
    private Map<String, String> doorMap, userMap;
    private RadioGroup durationRadioGroup;

    private long durationInSeconds = -1; // Duration in seconds (initialized to an invalid value)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_over_key);

        doorListSpinner = findViewById(R.id.doorListSpinner);
        userListSpinner = findViewById(R.id.userListSpinner);
        sendKeyButton = findViewById(R.id.sendKeyButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
        durationRadioGroup = findViewById(R.id.durationRadioGroup);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        doorList = new ArrayList<>();
        userList = new ArrayList<>();
        doorMap = new HashMap<>();
        userMap = new HashMap<>();

        fetchDoors();
        fetchUsers();

        // Radio group listener to track the selected duration
        durationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.oneHourRadioButton) {
                durationInSeconds = 3600; // 1 hour in seconds
            } else if (checkedId == R.id.oneDayRadioButton) {
                durationInSeconds = 86400; // 1 day in seconds
            } else if (checkedId == R.id.oneWeekRadioButton) {
                durationInSeconds = 604800; // 1 week in seconds
            } else if (checkedId == R.id.oneMonthRadioButton) {
                durationInSeconds = 2628000; // 1 month (30 days) in seconds
            }
        });



        sendKeyButton.setOnClickListener(v -> handOverKey());
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchDoors() {
        String nic = getIntent().getStringExtra("nic");
        if (nic == null) return;

        databaseReference.child(nic).child("keys").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String keyUid = snapshot.child("UUID").getValue(String.class);
                    String doorName = snapshot.child("door_name").getValue(String.class);
                    if (keyUid != null && doorName != null) {
                        doorMap.put(doorName, keyUid);
                        doorList.add(doorName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, doorList);
                doorListSpinner.setAdapter(adapter);
            }
        });
    }

    private void fetchUsers() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String nic = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    if (nic != null && name != null) {
                        userMap.put(name, nic);
                        userList.add(nic + " - " + name);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, userList);
                userListSpinner.setAdapter(adapter);
            }
        });
    }

    private void handOverKey() {
        String selectedDoor = doorListSpinner.getText().toString();
        String selectedUser = userListSpinner.getText().toString();

        if (selectedDoor.isEmpty() || selectedUser.isEmpty()) {
            Toast.makeText(this, "Please select both a door and a user", Toast.LENGTH_SHORT).show();
            return;
        }

        if (durationRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show();
            return;
        }

        String keyUid = doorMap.get(selectedDoor);
        String selectedUserNic = selectedUser.split(" - ")[0];

        if (keyUid == null || selectedUserNic.isEmpty()) return;

        String nic = getIntent().getStringExtra("nic");
        if (nic == null) return;

        databaseReference.child(nic).child("keys").child(keyUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String uuid = task.getResult().child("UUID").getValue(String.class);
                String doorId = task.getResult().child("door_id").getValue(String.class);
                String doorName = task.getResult().child("door_name").getValue(String.class);

                if (uuid != null && doorId != null && doorName != null) {
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("UUID", uuid);
                    keyData.put("door_id", doorId);
                    keyData.put("door_name", doorName);
                    keyData.put("expiration_time", System.currentTimeMillis() + (durationInSeconds * 1000));
                    //keyData.put("expiration_time", com.google.firebase.database.ServerValue.TIMESTAMP);


                    DatabaseReference userKeysRef = databaseReference.child(selectedUserNic).child("keys").child(uuid);
                    userKeysRef.setValue(keyData).addOnSuccessListener(aVoid -> {
                        Toast.makeText(HandOverKey.this, "Key send successfully", Toast.LENGTH_SHORT).show();
                        startExpirationTimer(userKeysRef, keyData);
                    }).addOnFailureListener(e -> Toast.makeText(HandOverKey.this, "Failed to send key", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void startExpirationTimer(DatabaseReference userKeysRef, Map<String, Object> keyData) {
        // Timer to check when the key should be removed
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Check if the current time has passed the expiration time
            Long expirationTime = (Long) keyData.get("expiration_time");
            if (expirationTime != null && System.currentTimeMillis() >= expirationTime) {
                // Remove the key from the receiver's key list
                userKeysRef.removeValue().addOnSuccessListener(aVoid ->
                        Toast.makeText(HandOverKey.this, "Key removed after expiration", Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(HandOverKey.this, "Failed to remove key", Toast.LENGTH_SHORT).show()
                );
            }
        }, durationInSeconds * 1000); // Delay for the selected duration
    }
}
