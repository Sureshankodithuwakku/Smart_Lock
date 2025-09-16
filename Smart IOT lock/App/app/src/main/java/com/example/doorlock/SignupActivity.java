package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput, confirmPasswordInput, nameInput, nicInput;
    private Spinner roleSpinner;
    private Button signupButton, loginRedirectButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeUI();

        // Set up the Spinner with role options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        signupButton.setOnClickListener(v -> handleSignup());
        loginRedirectButton.setOnClickListener(v -> navigateToLogin());
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.emailInput);
        nicInput = findViewById(R.id.NICInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.Confirmpassword);
        nameInput = findViewById(R.id.name);
        roleSpinner = findViewById(R.id.roleSpinner);
        signupButton = findViewById(R.id.signupButton);
        loginRedirectButton = findViewById(R.id.loginRedirectButton);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    private void handleSignup() {
        String email = emailInput.getText().toString().trim();
        String nic = nicInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String userName = nameInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (!validateInput(email, nic, password, confirmPassword, userName)) return;

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                saveUserData(nic, userName, role, email);
                Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(SignupActivity.this, "Signup failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean validateInput(String email, String nic, String password, String confirmPassword, String userName) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return false;
        }
        if (!email.endsWith("cmb.ac.lk")) {
            emailInput.setError("Enter valid email");
            return false;
        }
        if (TextUtils.isEmpty(nic)) {
            nicInput.setError("NIC is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.matches(".*\\d.*")) {  // Regex to check for at least one digit
            passwordInput.setError("Password must contain at least one number");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }
        if (TextUtils.isEmpty(userName)) {
            nameInput.setError("Name is required");
            return false;
        }
        return true;
    }


    private void saveUserData(String nic, String userName, String role, String email) {
        // Create a HashMap to store user details
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("role", role);
        userData.put("email", email);
        userData.put("keys", ""); // Placeholder for future keys if needed

        // Save the user data under the NIC node in Firebase Realtime Database
        database.child("Users").child(nic).setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignupActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(SignupActivity.this, "Failed to save user data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
