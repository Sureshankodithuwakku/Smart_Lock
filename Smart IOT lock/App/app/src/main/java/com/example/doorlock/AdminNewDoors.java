package com.example.doorlock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNewDoors extends AppCompatActivity {

    private EditText doorNameInput;
    private Button addDoorButton, backButton;
    private ProgressBar progressBar;
    private Spinner keyListSpinner;
    private DatabaseReference doorsReference, doorNamesReference;
    private List<String> doorIds;
    private String selectedDoorId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_doors);

        // Initialize Firebase Database references
        doorsReference = FirebaseDatabase.getInstance().getReference("Doors");
        doorNamesReference = FirebaseDatabase.getInstance().getReference("Door_Names");

        // Initialize UI elements
        doorNameInput = findViewById(R.id.keyHolderID);
        addDoorButton = findViewById(R.id.addKeyButton);
        backButton = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressBar);
        keyListSpinner = findViewById(R.id.keyListSpinner);

        // Load doors into Spinner
        loadDoorsIntoSpinner();

        // Set listener for add door button
        addDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDoor();
            }
        });

        // Set listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });

        // Spinner item selection listener
        keyListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignore default hint item
                    selectedDoorId = doorIds.get(position - 1);
                } else {
                    selectedDoorId = ""; // No valid selection
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDoorId = "";
            }
        });
    }

    /*
    private void loadDoorsIntoSpinner() {
        doorsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> doorNames = new ArrayList<>();
                doorIds = new ArrayList<>();
                doorNames.add("Select a door"); // Default hint item

                for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                    String doorId = doorSnapshot.getKey();
                    String doorName = doorSnapshot.child("name").getValue(String.class);
                    if (doorId != null && doorName != null) {
                        doorNames.add(doorName);
                        doorIds.add(doorId);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminNewDoors.this, android.R.layout.simple_spinner_dropdown_item, doorNames);
                keyListSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminNewDoors.this, "Failed to load doors", Toast.LENGTH_SHORT).show();
            }
        });
    }

     */

    private void loadDoorsIntoSpinner() {
        doorsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> doorNames = new ArrayList<>();
                doorIds = new ArrayList<>();
                doorNames.add("Select a door"); // Default hint item

                for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                    String doorUUID = doorSnapshot.getKey(); // This is the UUID
                    String doorId = doorSnapshot.child("door_id").getValue(String.class);

                    if (doorUUID != null && doorId != null) {
                        //String displayText = "ID: " + doorId + " (UUID: " + doorUUID + ")";
                        String displayText = "Door ID: " + doorId;
                        doorNames.add(displayText);
                        doorIds.add(doorUUID); // Store UUID as reference
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminNewDoors.this, android.R.layout.simple_spinner_dropdown_item, doorNames);
                keyListSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminNewDoors.this, "Failed to load doors", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addNewDoor() {
        String doorName = doorNameInput.getText().toString().trim();

        if (doorName.isEmpty()) {
            doorNameInput.setError("Please enter a door name");
            return;
        }

        if (selectedDoorId.isEmpty()) {
            Toast.makeText(this, "Please select a door", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Get the selected door's details from the "Doors" node
        doorsReference.child(selectedDoorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doorId = snapshot.child("door_id").getValue(String.class);

                    if (doorId != null) {
                        // Use the same UUID for both "Doors" and "Door_Names"
                        Map<String, Object> doorData = new HashMap<>();
                        doorData.put("UUID", selectedDoorId);
                        doorData.put("door_id", doorId);
                        doorData.put("door_name", doorName);

                        doorNamesReference.child(selectedDoorId).setValue(doorData).addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminNewDoors.this, "Door added successfully", Toast.LENGTH_SHORT).show();
                                doorNameInput.setText("");
                            } else {
                                Toast.makeText(AdminNewDoors.this, "Failed to add door", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AdminNewDoors.this, "Door ID not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminNewDoors.this, "Selected door not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminNewDoors.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
