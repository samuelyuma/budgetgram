package com.example.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraPageActivity extends AppCompatActivity implements LifecycleOwner {
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private ImageButton captureButton;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private Preview preview;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_page);

        TextView textLocation = findViewById(R.id.text_location);
        previewView = findViewById(R.id.camera);
        captureButton = findViewById(R.id.camera_button);

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_REQUEST_CODE);

        LocationHelper.fetchAndDisplayPlaceName(this, textLocation);

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        captureButton.setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                Log.d("CameraX", "Getting camera provider");
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error getting camera provider", e);
                Toast.makeText(this, "Error starting camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            preview = new Preview.Builder()
                    .build();

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            Log.d("CameraX", "Unbinding all use cases");
            cameraProvider.unbindAll();

            Log.d("CameraX", "Starting to bind preview");
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
            );
            Log.d("CameraX", "Preview bound successfully");

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

        } catch (Exception e) {
            Log.e("CameraX", "Error binding preview", e);
            e.printStackTrace();
            String errorMessage = "Error binding camera: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e("CameraX", "ImageCapture is null");
            return;
        }

        File outputDir = getExternalFilesDir(null);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File photoFile = new File(outputDir,
                "photo_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        Log.d("CameraX", "Taking picture");
        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("CameraX", "Picture taken successfully");
                        Intent intent = new Intent(
                                CameraPageActivity.this,
                                CamPreviewActivity.class
                        );
                        intent.putExtra("IMAGE_PATH", photoFile.getAbsolutePath());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Error taking picture", exception);
                        Toast.makeText(CameraPageActivity.this,
                                "Error capturing image: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CameraX", "Camera permission granted");
                startCamera();
            } else {
                Log.e("CameraX", "Camera permission denied");
                Toast.makeText(this, "Camera permission is required",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}