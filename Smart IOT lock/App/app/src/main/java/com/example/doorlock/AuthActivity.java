package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up biometric authentication
        setupBiometricPrompt();
        authenticateUser();
    }

    // Method to initialize and show the fingerprint authentication
    private void authenticateUser() {
        biometricPrompt.authenticate(promptInfo);
    }

    // Setup biometric prompt
    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(AuthActivity.this, "Authentication Error: " + errString, Toast.LENGTH_SHORT).show();
                finish();  // Close the app on authentication error
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(AuthActivity.this, "Authentication Succeeded", Toast.LENGTH_SHORT).show();
                // Proceed to MainActivity
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();  // Close AuthActivity
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AuthActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure the biometric prompt
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Authenticate to access the app")
                .setDeviceCredentialAllowed(true)  // Allows PIN/password as fallback
                .build();
    }
}
