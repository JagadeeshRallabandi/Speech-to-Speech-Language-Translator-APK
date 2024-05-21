package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class YourProfileActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        // Get username from intent extras
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        TextView tvYourDetails = findViewById(R.id.tvYourDetails);
        TextView tvYourTranslations = findViewById(R.id.tvYourTranslations);

        // Set click listener for Your Details button
        tvYourDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to YourDetailsActivity and pass the username
                Intent detailsIntent = new Intent(YourProfileActivity.this, YourDetailsActivity.class);
                detailsIntent.putExtra("username", username);
                startActivity(detailsIntent);
            }
        });

        // Set click listener for Your Translations button
        tvYourTranslations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to YourTranslationsActivity
                Intent translationsIntent = new Intent(YourProfileActivity.this, YourTranslationsActivity.class);
                translationsIntent.putExtra("username", username);
                startActivity(translationsIntent);
            }
        });
    }
}
