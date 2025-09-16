package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doorlock.Model.Key;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminAddKeys extends AppCompatActivity {

    private Button addKeyButton, backButton;
    private EditText userIdEditText;
    private Spinner doorListSpinner;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private ArrayList<String> doorList;
    private Map<String, String> doorMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_keys);

        userIdEditText = findViewById(R.id.keyHolderID);
        doorListSpinner = findViewById(R.id.doorListSpinner);
        addKeyButton = findViewById(R.id.addKeyButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent.hasExtra("nic")) {
            String nic = intent.getStringExtra("nic");
            userIdEditText.setText(nic);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        setupDoorSpinner();

        addKeyButton.setOnClickListener(v -> addKeyToUser());
        backButton.setOnClickListener(v -> finish());
    }

    private void setupDoorSpinner() {
        doorList = new ArrayList<>();
        doorMap = new HashMap<>();
        DatabaseReference doorsRef = FirebaseDatabase.getInstance().getReference("Door_Names");

        doorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doorList.clear();
                doorMap.clear();

                for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                    String doorDatabaseId = doorSnapshot.getKey();
                    String doorName = doorSnapshot.child("door_name").getValue(String.class);

                    if (doorName != null) {
                        doorList.add(doorName);
                        doorMap.put(doorDatabaseId, doorName);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminAddKeys.this, android.R.layout.simple_spinner_item, doorList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                doorListSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAddKeys.this, "Failed to load doors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addKeyToUser() {
        String userId = userIdEditText.getText().toString().trim();
        String selectedDoorName = doorListSpinner.getSelectedItem().toString();

        if (userId.isEmpty()) {
            Toast.makeText(this, "Please enter a User ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDoorName == null) {
            Toast.makeText(this, "Invalid door selection", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] selectedDoorUUID = {null}; // Using an array as a workaround
        DatabaseReference doorsRef = FirebaseDatabase.getInstance().getReference("Door_Names");

        doorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                    String doorDatabaseId = doorSnapshot.getKey();
                    String doorName = doorSnapshot.child("door_name").getValue(String.class);
                    String doorId = doorSnapshot.child("door_id").getValue(String.class);

                    if (doorName != null && doorName.equals(selectedDoorName)) {
                        selectedDoorUUID[0] = doorDatabaseId;

                        // Generate a unique UUID for the key under the user
                        String uniqueKeyId = selectedDoorUUID[0]; // Use the same UUID for Users and Door_Names
                        DatabaseReference userKeysRef = databaseReference.child(userId).child("keys").child(uniqueKeyId);

                        // Create a key map to store in Firebase
                        Map<String, Object> keyData = new HashMap<>();
                        keyData.put("UUID", uniqueKeyId);
                        keyData.put("door_id", doorId);
                        keyData.put("door_name", selectedDoorName);

                        progressBar.setVisibility(View.VISIBLE);

                        userKeysRef.setValue(keyData).addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminAddKeys.this, "Key added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminAddKeys.this, "Failed to add key", Toast.LENGTH_SHORT).show();
                            }
                        });

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAddKeys.this, "Failed to fetch door details", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
