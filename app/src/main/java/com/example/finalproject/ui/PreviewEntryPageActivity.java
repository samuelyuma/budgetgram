package com.example.finalproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;
import com.example.finalproject.database.Entry;
import com.example.finalproject.helpers.DatabaseHelper;

public class PreviewEntryPageActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private Entry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_entry_page);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        String locationName = intent.getStringExtra("location_name");
        String imageUrl = intent.getStringExtra("image_url");
        String addedDate = intent.getStringExtra("added_date");
        int price = intent.getIntExtra("price", 0);

        currentEntry = new Entry();
        currentEntry.setId(id);
        currentEntry.setLocationName(locationName);
        currentEntry.setImageUrl(imageUrl);
        currentEntry.setPrice(price);
        currentEntry.setAddedDate(addedDate);

        ImageButton backButton = findViewById(R.id.button_back);
        ImageButton deleteButton = findViewById(R.id.button_delete);
        ImageButton editButton = findViewById(R.id.button_edit);
        EditText priceEditInput = findViewById(R.id.price_edit_input);
        TextView locationText = findViewById(R.id.text_location);
        ImageView previewImage = findViewById(R.id.preview);
        TextView addedDateText = findViewById(R.id.added_date_text);

        addedDateText.setText(addedDate);
        locationText.setText(locationName);
        priceEditInput.setText(String.valueOf(price));
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(previewImage);

        backButton.setOnClickListener(v -> finish());

        deleteButton.setOnClickListener(v -> {
            new Thread(() -> {
                databaseHelper.deleteEntry(currentEntry);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }).start();
        });

        editButton.setOnClickListener(v -> {
            String newPriceStr = priceEditInput.getText().toString();
            if (newPriceStr.isEmpty()) {
                Toast.makeText(this, "Price cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int newPrice = Integer.parseInt(newPriceStr);
                currentEntry.setPrice(newPrice);

                new Thread(() -> {
                    databaseHelper.updateEntry(currentEntry);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Price updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                    });
                }).start();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            }
        });
    }
}