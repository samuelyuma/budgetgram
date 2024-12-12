package com.example.finalproject.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    @SuppressLint("MissingPermission")
    public static void fetchAndDisplayPlaceName(Context context, TextView textView) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            textView.setText("Permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String placeName = getPlaceNameFromLocation(context, location);
                updateTextView(textView, placeName);
            } else {
                textView.setText("Unable to get location");
            }
        }).addOnFailureListener(e -> textView.setText("Error: " + e.getMessage()));
    }

    private static String getPlaceNameFromLocation(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String street = address.getThoroughfare();
                if (street != null && !street.matches("[A-Z0-9+,-]+") && !street.toLowerCase().contains("jalan tanpa nama")) {
                    return street;
                }

                if (address.getSubLocality() != null) {
                    return address.getSubLocality();
                } else if (address.getLocality() != null) {
                    return address.getLocality();
                } else if (address.getSubAdminArea() != null) {
                    return address.getSubAdminArea();
                } else if (address.getAdminArea() != null) {
                    return address.getAdminArea();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Place";
    }

    public static void updateTextView(TextView textView, String text) {
        if (textView == null || text == null) return;

        textView.setText(text);

        textView.setSingleLine(true);
        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
    }
}
