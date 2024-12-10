package com.example.finalproject.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.finalproject.R;

public class PreviewEntryPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_entry_page);

        String locationName = getIntent().getStringExtra("location_name");
        String imageUrl = getIntent().getStringExtra("image_url");
        int price = getIntent().getIntExtra("price", 0);

        TextView locationText = findViewById(R.id.text_location);
        ImageView previewImage = findViewById(R.id.preview);
        TextView priceText = findViewById(R.id.text_price);
        ImageButton backButton = findViewById(R.id.button_back);

        locationText.setText(locationName);
        priceText.setText(String.format("%d", price));

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(previewImage);

        backButton.setOnClickListener(v -> finish());
    }
}