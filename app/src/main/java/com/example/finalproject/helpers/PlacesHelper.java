package com.example.finalproject.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.example.finalproject.R;

import java.util.Arrays;
import java.util.List;

public class PlacesHelper {
    private static final String TAG = "PlacesHelper";
    private static PlacesClient placesClient;

    public static void initPlaces(Context context, String apiKey) {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(context.getApplicationContext(), apiKey);
            }
            placesClient = Places.createClient(context);
            Log.d(TAG, "Places API initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Places API: " + e.getMessage());
            Toast.makeText(context, "Error initializing location services", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("MissingPermission")
    public static void showNearbyPlacesDialog(Context context, Location currentLocation, TextView textView) {
        if (placesClient == null) {
            Log.e(TAG, "PlacesClient is null. Trying to reinitialize...");
            initPlaces(context, context.getString(R.string.google_maps_key));

            if (placesClient == null) {
                Log.e(TAG, "Failed to initialize PlacesClient");
                Toast.makeText(context, "Location services unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        try {
            placesClient.findCurrentPlace(request)
                    .addOnSuccessListener(response -> {
                        List<PlaceLikelihood> placeLikelihoods = response.getPlaceLikelihoods();

                        if (placeLikelihoods.isEmpty()) {
                            Toast.makeText(context, "No nearby places found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] placeNames = new String[Math.min(5, placeLikelihoods.size())];
                        LatLng[] locations = new LatLng[Math.min(5, placeLikelihoods.size())];

                        for (int i = 0; i < Math.min(5, placeLikelihoods.size()); i++) {
                            Place place = placeLikelihoods.get(i).getPlace();
                            placeNames[i] = place.getName();
                            locations[i] = place.getLatLng();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select Nearby Location")
                                .setItems(placeNames, (dialog, which) -> {
                                    String selectedPlace = placeNames[which];
                                    textView.setText(selectedPlace);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                        builder.create().show();
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Error finding current place", exception);
                        Toast.makeText(context, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "Security exception: " + se.getMessage());
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(context, "Error accessing location services", Toast.LENGTH_SHORT).show();
        }
    }
}