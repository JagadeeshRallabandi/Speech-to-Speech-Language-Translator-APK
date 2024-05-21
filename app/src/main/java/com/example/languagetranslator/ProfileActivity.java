package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");


        // Set click listeners for profile and settings buttons
        TextView tvYourProfile = findViewById(R.id.tvYourProfile);
        TextView tvSettings = findViewById(R.id.tvSettings);



        tvYourProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to YourProfileActivity
                Intent intent = new Intent(ProfileActivity.this, YourProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        tvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SettingsActivity
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }
}
