package com.example.finalproject;

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

    /**
     * Fetches the current location and displays only the place name in the TextView.
     *
     * @param context   The application context.
     * @param textView  The TextView to update.
     */
    @SuppressLint("MissingPermission")
    public static void fetchAndDisplayPlaceName(Context context, TextView textView) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            textView.setText("Permission not granted");
            return;
        }

        // Get the last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String placeName = getPlaceNameFromLocation(context, location);
                updateTextView(textView, placeName);
            } else {
                textView.setText("Unable to get location");
            }
        }).addOnFailureListener(e -> textView.setText("Error: " + e.getMessage()));
    }

    /**
     * Converts a Location object to a place name (e.g., "Tunjungan Plaza").
     *
     * @param context  The application context.
     * @param location The Location object.
     * @return A string representing the place name.
     */
    private static String getPlaceNameFromLocation(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String placeName = addresses.get(0).getAddressLine(0);
                return placeName.substring(0, placeName.indexOf((",")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Place";
    }

    /**
     * Updates the TextView with the given text.
     *
     * @param textView The TextView to update.
     * @param text     The text to display.
     */
    public static void updateTextView(TextView textView, String text) {
        if (textView == null || text == null) return;

        textView.setText(text);

        // Configure the TextView to truncate if text exceeds one line
//        textView.setSingleLine(true);
//        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
    }
}
