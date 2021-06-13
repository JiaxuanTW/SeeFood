package com.nukcsie.nothotdog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        if (!hasCameraPermission())
            requestPermission();

        CardView nonRealTimeButton = findViewById(R.id.nonRealTImeButton);
        CardView realTimeButton = findViewById(R.id.realTimeButton);
        CardView shareButton = findViewById(R.id.shareButton);

        nonRealTimeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NonRealTimeActivity.class);
            startActivity(intent);
        });

        realTimeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RealTimeActivity.class);
            startActivity(intent);
        });

        shareButton.setOnClickListener(v -> share());
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, 4234);
    }

    private void share() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Share");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "See Food - The Shazam for Food");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
}