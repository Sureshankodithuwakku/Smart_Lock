package com.example.doorlock; // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doorlock.R;

public class Admin extends AppCompatActivity {

    private Button addKeyButton, detailsButton, addNewKeyButton, logViewButton, logOutAdminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Ensure your XML filename is "admin.xml"

        // Initialize buttons
        addKeyButton = findViewById(R.id.AddkeyButton);
        detailsButton = findViewById(R.id.DetailsButton);
        addNewKeyButton = findViewById(R.id.AddNewKeysButton);
        logViewButton = findViewById(R.id.logViewButton);
        logOutAdminButton = findViewById(R.id.logOutAdminButton);

        // Set click listeners
        addKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, AdminAddKeys.class);
                startActivity(intent);
            }
        });

        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, UserDetails.class);
                startActivity(intent);
            }
        });

        addNewKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, AdminNewDoors.class);
                startActivity(intent);
            }
        });

        logViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, LogActivity.class);
                startActivity(intent);
            }
        });


        logOutAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
