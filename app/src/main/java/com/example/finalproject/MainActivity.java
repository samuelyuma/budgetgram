package com.example.finalproject;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.views.MainButton;

public class MainActivity extends AppCompatActivity implements LocationHelper.LocationCallback {

    private TextView tvLocation;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = findViewById(R.id.TextViewLocation);
        MainButton btnGetLocation = findViewById(R.id.ButtonGetLocation);

        locationHelper = new LocationHelper(this);

        btnGetLocation.setOnClickListener(v -> locationHelper.requestLocation(this, this));
    }

    @Override
    public void onLocationRetrieved(String address) {
        tvLocation.setText(address);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.requestLocation(this, this);
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk mengakses lokasi.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}