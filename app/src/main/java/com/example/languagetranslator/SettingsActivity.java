package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Set click listeners for each button
        findViewById(R.id.tvResetPassword).setOnClickListener(v -> {
            // Start ResetPasswordActivity and pass username as extra
            Intent resetPasswordIntent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
            resetPasswordIntent.putExtra("username", username);
            startActivity(resetPasswordIntent);
        });

        findViewById(R.id.tvDeleteTranslations).setOnClickListener(v -> {
            // Start DeleteTranslationsActivity and pass username as extra
            Intent deleteTranslationsIntent = new Intent(SettingsActivity.this, DeleteTranslationsActivity.class);
            deleteTranslationsIntent.putExtra("username", username);
            startActivity(deleteTranslationsIntent);
        });

        TextView tvTermsAndConditions = findViewById(R.id.tvTermsAndConditions);
        tvTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, TnCActivity.class));
            }
        });

        findViewById(R.id.tvDeleteAccount).setOnClickListener(v -> {
            // Start DeleteAccountActivity and pass username as extra
            Intent deleteAccountIntent = new Intent(SettingsActivity.this, DeleteAccountActivity.class);
            deleteAccountIntent.putExtra("username", username);
            startActivity(deleteAccountIntent);
        });

        TextView tvAppVersion = findViewById(R.id.tvAppVersion);
        tvAppVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AppVersionActivity.class));
            }
        });
        TextView tvDefaultLanguage = findViewById(R.id.tvDefaultLanguage);
        tvDefaultLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, DefaultLanguageActivity.class));
            }
        });
        TextView tvAboutApp = findViewById(R.id.tvAboutApp);
        tvAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AboutAppActivity.class));
            }
        });


        TextView tvLogout = findViewById(R.id.tvLog_out);
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                mAuth.signOut();

                // Redirect to LoginActivity
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));

                // Finish the current activity to prevent going back to SettingsActivity after logout
                finish();
            }
        });
    }
}
