package com.example.doorlock;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {
    private ListView logListView;
    private EditText searchBar;
    private Button buttonClear, buttonBack;
    private ArrayAdapter<String> logAdapter;
    private List<String> logList;
    private List<String> filteredLogList;
    private DatabaseReference logRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logListView = findViewById(R.id.logListView);
        searchBar = findViewById(R.id.searchBarLogView);
        buttonClear = findViewById(R.id.buttonClear);
        buttonBack = findViewById(R.id.buttonBack);

        logList = new ArrayList<>();
        filteredLogList = new ArrayList<>();
        logAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredLogList);
        logListView.setAdapter(logAdapter);

        logRef = FirebaseDatabase.getInstance().getReference("log");

        fetchLogs();
        setupSearchBar();

        // Handle Back Button Click
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes the activity
            }
        });

        // Handle Clear Button Click
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLogs();
            }
        });
    }

    private void fetchLogs() {
        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logList.clear();

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String logMessage = logSnapshot.getValue(String.class);
                    if (logMessage != null) {
                        logList.add(logMessage); // Removed date and time formatting
                    }
                }
                filterLogs(searchBar.getText().toString()); // Apply search filter when data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LogActivity.this, "Failed to load logs!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterLogs(String query) {
        filteredLogList.clear();
        if (query.isEmpty()) {
            filteredLogList.addAll(logList);
        } else {
            for (String log : logList) {
                if (log.toLowerCase().contains(query.toLowerCase())) {
                    filteredLogList.add(log);
                }
            }
        }
        logAdapter.notifyDataSetChanged();
    }


    /*
    private void clearLogs() {
        logRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                logList.clear();
                filteredLogList.clear();
                logAdapter.notifyDataSetChanged();
                Toast.makeText(LogActivity.this, "Logs cleared successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LogActivity.this, "Failed to clear logs!", Toast.LENGTH_SHORT).show();
            }
        });
    }

     */

    private void clearLogs() {
        new AlertDialog.Builder(LogActivity.this)
                .setTitle("Confirm Clear Logs")
                .setMessage("Are you sure you want to clear all logs?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            logList.clear();
                            filteredLogList.clear();
                            logAdapter.notifyDataSetChanged();
                            Toast.makeText(LogActivity.this, "Logs cleared successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LogActivity.this, "Failed to clear logs!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
