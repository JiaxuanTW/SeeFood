package com.nukcsie.nothotdog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Size;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.nukcsie.nothotdog.utils.ImageAnalyzer;
import com.nukcsie.nothotdog.utils.ImageAnalyzer.ANALYSIS_MODE;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class RealTimeActivity extends AppCompatActivity {
    private Camera camera;
    private boolean isFlashOn = false;
    private ImageAnalyzer imageAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_real_time);
        imageAnalyzer = new ImageAnalyzer(this, ANALYSIS_MODE.REAL_TIME);
        startCamera();
    }

    @SuppressLint("SetTextI18n")
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageAnalyzer);

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                PreviewView previewView = findViewById(R.id.previewView);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        imageAnalyzer.setImageAnalyzerListener(recognitionItems -> runOnUiThread(() -> {
            ImageView imageView = findViewById(R.id.resultImage2);
            TextView resultValue = findViewById(R.id.resultValue);
            TextView value1 = findViewById(R.id.value1);
            TextView value2 = findViewById(R.id.value2);
            TextView value3 = findViewById(R.id.value3);

            value1.setText(recognitionItems.get(0).getConfidence() + "%");
            value2.setText(recognitionItems.get(1).getConfidence() + "%");
            value3.setText(recognitionItems.get(2).getConfidence() + "%");
            resultValue.setText(Collections.max(recognitionItems).label);

            if (Collections.max(recognitionItems).label.equals("Hot Dog"))
                imageView.setImageResource(R.drawable.hot_dog);
            else if (Collections.max(recognitionItems).label.equals("Not Hot Dog"))
                imageView.setImageResource(R.drawable.pizza);
            else
                imageView.setImageResource(R.drawable.dog);
        }));

        ImageView flashlight = findViewById(R.id.flashlight2);
        flashlight.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            flashlight.setImageResource(isFlashOn ?
                    R.drawable.ic_baseline_flash_off_24 :
                    R.drawable.ic_outline_flash_on_24);
        });
    }
}