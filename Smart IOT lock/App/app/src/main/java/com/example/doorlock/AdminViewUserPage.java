package com.example.doorlock;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doorlock.Adapter.AdminKeyAdapter;
import com.example.doorlock.Model.Key;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminViewUserPage extends AppCompatActivity {
    private TextView userName, userId, userEmail, userRole;
    private RecyclerView userKeysRecyclerView;
    private DatabaseReference databaseReference;
    private AdminKeyAdapter adapter;
    private List<Key> keyList;
    private String nic;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_user_page);

        userName = findViewById(R.id.userName);
        userId = findViewById(R.id.userId);
        userEmail = findViewById(R.id.userEmail);
        userRole = findViewById(R.id.userRole);
        userKeysRecyclerView = findViewById(R.id.userKeysRecyclerView);
        backButton = findViewById(R.id.back_Button);

        // Receiving data from LoginActivity
        nic = getIntent().getStringExtra("nic");
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");

        // Setting data to TextViews
        userName.setText("Name: " + name);
        userId.setText("NIC: " + nic);
        userEmail.setText("Email: " + email);
        userRole.setText("Role: " + role);

        // Set background color based on role
        setBackgroundColorByRole(role);

        // Fetching keys for the user
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(nic).child("keys");

        keyList = new ArrayList<>();
        adapter = new AdminKeyAdapter(keyList, nic, databaseReference);
        userKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userKeysRecyclerView.setAdapter(adapter);

        fetchKeys();
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchKeys() {
        keyList.clear();
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String keyUid = snapshot.child("UUID").getValue(String.class);
                    String doorId = snapshot.child("door_id").getValue(String.class);
                    String doorName = snapshot.child("door_name").getValue(String.class);

                    if (keyUid != null && doorId != null && doorName != null) {
                        keyList.add(new Key(keyUid, doorName, doorId)); // Store all details
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AdminViewUserPage.this, "No keys found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void fetchKeys() {
        keyList.clear();
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Key key = snapshot.getValue(Key.class);
                    if (key != null) {
                        keyList.add(new Key(snapshot.getKey(), key.getKeyName()));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AdminViewUserPage.this, "No keys found", Toast.LENGTH_SHORT).show();
            }
        });
    }

     */

    private void setBackgroundColorByRole(String role) {
        switch (role) {
            case "Lecturer":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.lecturer_bg);
                break;
            case "Student":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.student_bg);
                break;
            case "Staff":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.staff_bg);
                break;
            case "Other":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.others_bg);
                break;
            default:
                findViewById(R.id.userPageLayout).setBackgroundColor(Color.WHITE);
                break;
        }
    }
}
