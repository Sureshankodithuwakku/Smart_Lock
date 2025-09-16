package com.example.doorlock;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doorlock.Adapter.UserAdapter;
import com.example.doorlock.Model.User;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class UserDetails extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList, filteredList;
    private DatabaseReference databaseReference;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchBar = findViewById(R.id.searchBar);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(this, filteredList);
        recyclerView.setAdapter(userAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchUsers();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String nic = snapshot.getKey(); // NIC as the key
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);
                String id = "";

                if (name != null && email != null && role != null) {
                    User user = new User(id, name, email, role, nic);
                    userList.add(user);
                    userAdapter.notifyItemInserted(userList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserDetails", "Failed to fetch users: " + error.getMessage());
            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String nic = userSnapshot.getKey();
                    String name = userSnapshot.child("name").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String role = userSnapshot.child("role").getValue(String.class);
                    String id = "";

                    if (name != null && email != null && role != null) {
                        User user = new User(id, name, email, role, nic);
                        userList.add(user);
                    }
                }
                filterUsers("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserDetails", "Failed to fetch users: " + error.getMessage());
            }
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getNic().toLowerCase().contains(query.toLowerCase()) ||
                        user.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}