package com.nukcsie.nothotdog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.nukcsie.nothotdog.models.RecognitionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ResultsActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_results);

        ArrayList<RecognitionItem> recognitionItems =
                getIntent().getParcelableArrayListExtra("results");
        ImageView imageView = findViewById(R.id.resultImage);
        TextView result = findViewById(R.id.resultLabel);
        TextView label1 = findViewById(R.id.label1);
        TextView label2 = findViewById(R.id.label2);
        TextView label3 = findViewById(R.id.label3);

        Collections.sort(recognitionItems);
        Collections.reverse(recognitionItems);

        if (recognitionItems.get(0).label.equals("Hot Dog"))
            imageView.setImageResource(R.drawable.hot_dog);
        else if (recognitionItems.get(0).label.equals("Not Hot Dog"))
            imageView.setImageResource(R.drawable.pizza);
        else
            imageView.setImageResource(R.drawable.dog);

        result.setText(recognitionItems.get(0).label);
        label1.setText(recognitionItems.get(0).label + ": "
                + recognitionItems.get(0).getConfidence() + "%");
        label2.setText(recognitionItems.get(1).label + ": "
                + recognitionItems.get(1).getConfidence() + "%");
        label3.setText(recognitionItems.get(2).label + ": "
                + recognitionItems.get(2).getConfidence() + "%");

        CardView shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> share(recognitionItems.get(0).label));
    }

    private void share(String item) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Share");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I found a " + item);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
}