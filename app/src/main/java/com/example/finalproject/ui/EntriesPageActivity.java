package com.example.finalproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.ImageButton;
import com.example.finalproject.ui.CameraPageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.EntryAdapter;
import com.example.finalproject.database.Entry;
import com.example.finalproject.helpers.DatabaseHelper;
import java.util.List;

public class EntriesPageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntryAdapter entryAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entries_page);

        recyclerView = findViewById(R.id.recyclerView_entries);
        databaseHelper = new DatabaseHelper(this);

        ImageButton buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(view -> {
            Intent intent = new Intent(EntriesPageActivity.this, CameraPageActivity.class);
            startActivity(intent);
        });

        loadEntries();
    }

    private void loadEntries() {
        new Thread(() -> {
            List<Entry> entries = databaseHelper.getAllEntries();

            runOnUiThread(() -> {
                TextView textNoEntries = findViewById(R.id.text_no_entries);
                RecyclerView recyclerView = findViewById(R.id.recyclerView_entries);

                if (entries.isEmpty()) {
                    textNoEntries.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textNoEntries.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    entryAdapter = new EntryAdapter(this, entries);
                    recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
                    recyclerView.setAdapter(entryAdapter);
                }
            });
        }).start();
    }

}
