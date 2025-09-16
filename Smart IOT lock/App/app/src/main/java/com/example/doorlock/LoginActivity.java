package com.example.doorlock;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton,signupButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        signupButton = findViewById(R.id.signupButton);


        loginButton.setOnClickListener(v -> loginUser());

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }


    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();


        // Check if the device has an internet connection
        if (!isConnectedToInternet()) {
            Toast.makeText(LoginActivity.this, "No internet connection. Please check your connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email and password are empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email and password are admin credentials
        if (email.equals("admin") && password.equals("admin")) {
            Intent intent = new Intent(LoginActivity.this, Admin.class);
            startActivity(intent);
            return; // Skip the rest of the login process if it's admin
        }

        // Continue with Firebase authentication for other users
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fetchUserDetails(email);
            } else {
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to check internet connectivity
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }



    private void fetchUserDetails(String email) {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String storedEmail = snapshot.child("email").getValue(String.class);
                    if (storedEmail != null && storedEmail.equals(email)) {
                        // Found the user, extract details
                        String nic = snapshot.getKey();  // Get NIC (Primary Key)
                        String name = snapshot.child("name").getValue(String.class);
                        String role = snapshot.child("role").getValue(String.class);

                        // Send data to UserPage
                        Intent intent = new Intent(LoginActivity.this, UserPage.class);
                        intent.putExtra("name", name);
                        intent.putExtra("nic", nic);
                        intent.putExtra("email", storedEmail);
                        intent.putExtra("role", role);
                        startActivity(intent);
                        return; // Exit loop once the user is found
                    }
                }
                // If no matching email was found
                Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
