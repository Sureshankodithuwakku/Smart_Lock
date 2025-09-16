package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Users extends AppCompatActivity {

    private ListView nameListView;
    private Button logoutButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ArrayList<String> nameList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Initialize views
        nameListView = findViewById(R.id.nameListView);
        progressBar = findViewById(R.id.progressBar);
        logoutButton = findViewById(R.id.LectureButton); // Rename button ID in XML if needed

        // Firebase authentication and database reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize list and adapter
        nameList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);
        nameListView.setAdapter(adapter);

        // Load user names
        loadUserNames();

        // Set up logout button
        logoutButton.setOnClickListener(v -> logout());

        // Set up item click listener for ListView
        nameListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = nameList.get(position);

            // Pass the selected key to Hand_over_key activity
            Intent intent = new Intent(Users.this, UserDetails.class);
            intent.putExtra("selectedKey", selectedName); // Passing the key as an extra
            startActivity(intent);
        });
    }

    private void loadUserNames() {
        progressBar.setVisibility(View.VISIBLE);

        // Reference the "Users" node in the database
        DatabaseReference usersRef = databaseReference.child("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                nameList.clear(); // Clear the list before adding new data

                // Iterate over all users in the "Users" node
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String name = userSnapshot.child("name").getValue(String.class); // Fetch the "name" field
                    if (name != null) {
                        nameList.add(name); // Add the name to the list
                    }
                }

                // Notify the adapter about data changes
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (nameList.isEmpty()) {
                    Toast.makeText(Users.this, "No names found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Users.this, "Failed to load names: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Users.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}