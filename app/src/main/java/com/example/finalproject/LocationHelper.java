package com.example.finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public interface LocationCallback {
        void onLocationRetrieved(String address);
    }

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void requestLocation(Activity activity, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation(callback);
        }
    }

    private void getLastLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Izin lokasi belum diberikan", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getAddressFromLocation(latitude, longitude, callback);
                        } else {
                            Toast.makeText(context, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude, LocationCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String street = address.getThoroughfare();
                String subLocality = address.getSubLocality();
                String locality = address.getLocality();
                String city = address.getSubAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();

                String fullAddress = String.format("%s, %s, %s, %s, %s, %s",
                        street != null ? street : "N/A",
                        subLocality != null ? subLocality : "N/A",
                        locality != null ? locality : "N/A",
                        city != null ? city : "N/A",
                        country != null ? country : "N/A",
                        postalCode != null ? postalCode : "N/A");

                callback.onLocationRetrieved(fullAddress);
            } else {
                callback.onLocationRetrieved("Alamat tidak tersedia.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Tidak dapat mengonversi lokasi ke alamat", Toast.LENGTH_SHORT).show();
        }
    }
}
