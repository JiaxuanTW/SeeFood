package com.nukcsie.nothotdog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.nukcsie.nothotdog.models.RecognitionItem;
import com.nukcsie.nothotdog.utils.ImageAnalyzer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonRealTimeActivity extends AppCompatActivity {
    private Camera camera;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_non_real_time);
        startCamera();
    }

    private Context getContext() {
        return this;
    }

    private void startCamera() {
        ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                @SuppressLint("RestrictedApi")
                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(224, 224))
                        .setBufferFormat(ImageFormat.YUV_420_888)
                        .setTargetRotation(Surface.ROTATION_0)
                        .build();

                findViewById(R.id.captureButton).setOnClickListener(view -> imageCapture.takePicture(cameraExecutor,
                        new ImageCapture.OnImageCapturedCallback() {
                            @Override
                            public void onCaptureSuccess(@NonNull @NotNull ImageProxy image) {
                                super.onCaptureSuccess(image);
                                ImageAnalyzer imageAnalyzer = new ImageAnalyzer(getContext(),
                                        ImageAnalyzer.ANALYSIS_MODE.NON_REAL_TIME);
                                imageAnalyzer.analyze(image);
                                startResultsActivity(imageAnalyzer.getRecognitionOutputs());
                            }
                        }));

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                PreviewView previewView = findViewById(R.id.previewView);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        ImageView flashlight = findViewById(R.id.flashlight);
        flashlight.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            flashlight.setImageResource(isFlashOn ?
                    R.drawable.ic_baseline_flash_off_24 :
                    R.drawable.ic_outline_flash_on_24);
        });
    }

    private void startResultsActivity(ArrayList<RecognitionItem> recognitionItems) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("results", recognitionItems);
        startActivity(intent);
    }
}