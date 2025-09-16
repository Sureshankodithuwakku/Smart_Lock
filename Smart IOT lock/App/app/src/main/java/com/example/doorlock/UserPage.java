package com.example.doorlock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doorlock.Adapter.KeyAdapter;
import com.example.doorlock.Model.Key;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserPage extends AppCompatActivity {
    private TextView userName, userId, userEmail, userRole;
    private RecyclerView userKeysRecyclerView;
    private DatabaseReference databaseReference;
    private KeyAdapter adapter;
    private List<Key> keyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        userName = findViewById(R.id.userName);
        userId = findViewById(R.id.userId);
        userEmail = findViewById(R.id.userEmail);
        userRole = findViewById(R.id.userRole);
        userKeysRecyclerView = findViewById(R.id.userKeysRecyclerView);

        // Receiving data from LoginActivity
        String name = getIntent().getStringExtra("name");
        String nic = getIntent().getStringExtra("nic");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");

        // Setting data to TextViews
        userName.setText("Name: "+ name);
        userId.setText("NIC: "+nic);
        userEmail.setText("Email: "+email);
        userRole.setText("Role: "+role);

        // Set background color based on the role
        setBackgroundColorByRole(role);

        // Fetching keys for the user
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(nic).child("keys");

        keyList = new ArrayList<>();
        //adapter = new KeyAdapter(keyList);
        adapter = new KeyAdapter(keyList, nic); // Pass the NIC to KeyAdapter

        userKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userKeysRecyclerView.setAdapter(adapter);

        fetchKeys();

        Button logOutButton = findViewById(R.id.userLogOutButton);
        Button sendKeyButton = findViewById(R.id.sendKeyButton);


        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserPage.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        sendKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserPage.this, HandOverKey.class);
                intent.putExtra("nic", nic); // Pass user NIC to the next activity if needed
                startActivity(intent);
            }
        });

    }


    private void fetchKeys() {
        keyList.clear(); // Ensure old data is cleared

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
                adapter.notifyDataSetChanged(); // Notify adapter after updating list
            } else {
                Toast.makeText(UserPage.this, "No keys found", Toast.LENGTH_SHORT).show();
            }
        });
    }




    //6
    /*
    private void fetchKeys() {
        keyList.clear(); // Ensure old data is cleared before adding new data
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String keyUid = snapshot.child("keyUid").getValue(String.class);
                    String keyName = snapshot.child("keyName").getValue(String.class);

                    if (keyUid != null && keyName != null) {
                        keyList.add(new Key(keyUid, keyName)); // Store UUID & Name
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(UserPage.this, "No keys found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    */





    // Method to change the background color based on the role
    private void setBackgroundColorByRole(String role) {
        switch (role) {
            case "Lecturer":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.lecturer_bg); // lecturer
                break;
            case "Student":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.student_bg); // student
                break;
            case "Staff":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.staff_bg); // staff
                break;
            case "Other":
                findViewById(R.id.userPageLayout).setBackgroundResource(R.drawable.others_bg); // other
                break;
            default:
                findViewById(R.id.userPageLayout).setBackgroundColor(Color.WHITE); // Default to white
                break;
        }
    }
}
