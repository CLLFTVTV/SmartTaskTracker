package com.example.smarttasktracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    TaskAdapter activeAdapter, historyAdapter;
    RecyclerView recyclerActive, recyclerHistory;
    Spinner spinnerSort;
    int currentSortMode = 0; // Default: Name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recyclerActive = findViewById(R.id.recyclerActive);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        spinnerSort = findViewById(R.id.spinnerSort); // Find the spinner

        // --- 1. SETUP SPINNER ---
        String[] sortOptions = {"Sort by Name", "Sort by Cost (High-Low)", "Sort by Time (Shortest)"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortMode = position;
                loadTasks(); // Reload lists with new sort order
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- 2. SETUP ADAPTERS ---
        TaskAdapter.OnTaskActionListener actionListener = new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onStart(int id) {
                db.startTask(id);
                loadTasks();
            }

            @Override
            public void onFinish(int id, long elapsedSeconds) {
                db.completeTask(id, elapsedSeconds);
                loadTasks();
            }

            @Override
            public void onDelete(int id) {
                db.deleteTask(id);
                loadTasks();
            }
        };

        recyclerActive.setLayoutManager(new LinearLayoutManager(this));
        activeAdapter = new TaskAdapter(new ArrayList<>(), false, actionListener);
        recyclerActive.setAdapter(activeAdapter);

        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new TaskAdapter(new ArrayList<>(), true, actionListener);
        recyclerHistory.setAdapter(historyAdapter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddTaskDialog());
        findViewById(R.id.btnAnalytics).setOnClickListener(v -> startActivity(new Intent(this, AnalysisActivity.class)));

        // Initial load is handled by the Spinner listener, but safe to call if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        // Pass 'currentSortMode' to the DB helper
        activeAdapter.updateList(db.getTasks(false, currentSortMode));
        historyAdapter.updateList(db.getTasks(true, currentSortMode));
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etCost = view.findViewById(R.id.etCost);
        EditText etMinutes = view.findViewById(R.id.etMinutes);
        etMinutes.setHint("Expected Mins");

        builder.setTitle("New Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String costStr = etCost.getText().toString();
                    String minStr = etMinutes.getText().toString();

                    if (!name.isEmpty() && !costStr.isEmpty() && !minStr.isEmpty()) {
                        db.addTask(name, Double.parseDouble(costStr), Integer.parseInt(minStr));
                        loadTasks();
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}