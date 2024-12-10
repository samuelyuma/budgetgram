package com.example.finalproject.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.finalproject.R;
import com.example.finalproject.helpers.LocationHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraPageActivity extends AppCompatActivity implements LifecycleOwner {
    private static final String TAG = "CameraPageActivity";
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private PreviewView previewView;
    private ImageButton captureButton;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private Preview preview;
    private ImageCapture imageCapture;
    private boolean permissionDeniedPermanently = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_page);

        initializeViews();
        checkAndRequestPermissions();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.camera);
        captureButton = findViewById(R.id.camera_button);
        TextView textLocation = findViewById(R.id.text_location);
        ImageButton buttonEntries = findViewById(R.id.button_entries);

        captureButton.setOnClickListener(v -> takePhoto());
        buttonEntries.setOnClickListener(v -> {
            Intent intent = new Intent(CameraPageActivity.this, EntriesPageActivity.class);
            startActivity(intent);
        });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            // All permissions are granted
            setupCamera();
            LocationHelper.fetchAndDisplayPlaceName(this, findViewById(R.id.text_location));
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            boolean shouldShowRationale = false;

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    // Check if user clicked "Don't ask again"
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        permissionDeniedPermanently = true;
                    } else {
                        shouldShowRationale = true;
                    }
                }
            }

            if (allGranted) {
                setupCamera();
                LocationHelper.fetchAndDisplayPlaceName(this, findViewById(R.id.text_location));
            } else if (permissionDeniedPermanently) {
                showSettingsDialog();
            } else if (shouldShowRationale) {
                showPermissionExplanationDialog();
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Needed")
                .setMessage("This app needs camera and location permissions to capture photos and add location information. Please grant these permissions to continue.")
                .setPositiveButton("Try Again", (dialog, which) -> checkAndRequestPermissions())
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("Some permissions are permanently denied. Please enable them in Settings to use this app.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera: ", e);
                Toast.makeText(this, "Error setting up camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            preview = new Preview.Builder().build();

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            cameraProvider.unbindAll();

            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
            );

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

        } catch (Exception e) {
            Log.e(TAG, "Error binding camera preview: ", e);
            Toast.makeText(this, "Error setting up camera preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera is not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputDir = new File(getExternalFilesDir(null), "Photos");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File photoFile = new File(outputDir, "photo_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Intent intent = new Intent(CameraPageActivity.this, CamPreviewActivity.class);
                        intent.putExtra("IMAGE_PATH", photoFile.getAbsolutePath());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error taking photo: ", exception);
                        Toast.makeText(CameraPageActivity.this,
                                "Failed to take photo: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}