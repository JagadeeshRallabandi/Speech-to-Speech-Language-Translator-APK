package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize TextViews
        TextView loginTextView = findViewById(R.id.tv_login);
        TextView aboutUsTextView = findViewById(R.id.tv_about_us);
        Button signupButton = findViewById(R.id.btn_signup);

        // Set click listeners for TextViews
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                // For example, navigate to login activity
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });

        aboutUsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle about us button click
                // For example, show about us information
                startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle signup button click
                // For example, navigate to signup activity
                startActivity(new Intent(HomeActivity.this, SignupActivity.class));
            }
        });
    }
}
