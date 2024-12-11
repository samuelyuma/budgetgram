package com.example.finalproject.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.finalproject.R;
import com.example.finalproject.helpers.DatabaseHelper;
import com.example.finalproject.helpers.LocationHelper;
import com.example.finalproject.helpers.PlacesHelper;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class CamPreviewActivity extends AppCompatActivity {
    private static final String TAG = "CamPreviewActivity";
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private String imagePath;
    private EditText priceInput;
    private ImageView previewImage;
    private TextView locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_preview_page);

        // Request necessary permissions
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE);

        // Initialize Places API
        try {
            String apiKey = getString(R.string.google_maps_key);
            PlacesHelper.initPlaces(this, apiKey);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Places API", e);
            Toast.makeText(this, "Error initializing location services", Toast.LENGTH_SHORT).show();
        }

        initializeViews();
        setupLocationPicker();
        setupPriceInput();

        imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath == null) {
            Log.e(TAG, "No image path provided");
            Toast.makeText(this, "Error: No image path provided", Toast.LENGTH_SHORT).show();
            navigateToCamera();
            return;
        }

        setClickListeners();
        loadImage();
    }

    private void navigateToCamera() {
        Intent intent = new Intent(this, CameraPageActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        previewImage = findViewById(R.id.preview);
        ImageButton backButton = findViewById(R.id.button_back);
        ImageButton addButton = findViewById(R.id.button_add);
        priceInput = findViewById(R.id.price_input);
        locationText = findViewById(R.id.text_location);
    }

    private void setupLocationPicker() {
        View locationLayout = findViewById(R.id.text_loc);
        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermissions()) {
                    LocationHelper.fetchAndDisplayPlaceName(CamPreviewActivity.this, locationText);
                } else {
                    requestLocationPermissions();
                }
            }
        });
    }

    private void setupPriceInput() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i)) &&
                            source.charAt(i) != ' ') {
                        return "";
                    }
                }
                return null;
            }
        };

        priceInput.setFilters(new InputFilter[]{filter});

        priceInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !priceInput.getText().toString().isEmpty()) {
                formatPrice();
            }
        });
    }

    private void formatPrice() {
        try {
            String input = priceInput.getText().toString().replaceAll("\\s+", "");
            double amount = Double.parseDouble(input);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            String formatted = formatter.format(amount);
            priceInput.setText(formatted);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error formatting price", e);
        }
    }

    private void setClickListeners() {
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCamera();
            }
        });

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString().trim().replaceAll("\\s+", "");
                if (validateInput(price)) {
                    saveEntry(imagePath, price, locationText.getText().toString());
                }
            }
        });
    }

    private boolean checkLocationPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE);
    }

    private void loadImage() {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                Log.e(TAG, "Image file does not exist: " + imagePath);
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024);

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            if (bitmap != null) {
                ExifInterface exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                Matrix matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                previewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                previewImage.setImageBitmap(bitmap);
            } else {
                Log.e(TAG, "Failed to decode bitmap");
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                navigateToCamera();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            navigateToCamera();
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean validateInput(String price) {
        if (price.isEmpty()) {
            Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveEntry(String imagePath, String price, String location) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.insertEntry(location, imagePath, price);
            Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show();
            navigateToCamera();
        } catch (Exception e) {
            Log.e(TAG, "Error saving entry", e);
            Toast.makeText(this, "Error saving entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationHelper.fetchAndDisplayPlaceName(this, locationText);
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        navigateToCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewImage != null) {
            Bitmap bitmap = null;
            if (previewImage.getDrawable() instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) previewImage.getDrawable()).getBitmap();
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            previewImage.setImageBitmap(null);
        }
    }
}